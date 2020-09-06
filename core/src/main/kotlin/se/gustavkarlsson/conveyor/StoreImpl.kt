package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State>(
    initialState: State,
    startActions: Iterable<Action<State>> = emptyList(),
    liveActions: Iterable<Action<State>> = emptyList(),
    commandBufferSize: Int = DEFAULT_BUFFER_SIZE,
) : Store<State> {

    private val stateHolder = StateHolder(initialState)

    private val commandProcessor = CommandProcessor(commandBufferSize, stateHolder::get, stateHolder::set)

    private val startActionsProcessor = StartActionsProcessor(startActions, commandProcessor)

    private val liveActionsProcessor = LiveActionsProcessor(liveActions, commandProcessor)

    override val state = stateHolder.flow
        .onStart { liveActionsProcessor.increaseLiveCount() }
        .onCompletion { liveActionsProcessor.decreaseLiveCount() }

    override val currentState get() = stateHolder.get()

    private val stage = AtomicReference(Stage.Initial)

    override fun start(scope: CoroutineScope): Job {
        if(!stage.compareAndSet(Stage.Initial, Stage.Started)) {
            throw StoreStartedException
        }
        val job = scope.launch {
            launch { commandProcessor.process(scope) }
            launch { startActionsProcessor.process(scope) }
            launch { liveActionsProcessor.process(scope) }
        }
        job.invokeOnCompletion { throwable ->
            stage.set(Stage.Stopped)
            liveActionsProcessor.close(throwable)
            commandProcessor.close(throwable)
            stateHolder.close(throwable)
        }
        return job
    }

    override suspend fun issue(command: Command<State>) {
        if(stage.get() == Stage.Stopped) {
            throw StoreStoppedException
        }
        commandProcessor.issue(command)
    }

    private enum class Stage { Initial, Started, Stopped }
}

@FlowPreview
@ExperimentalCoroutinesApi
private class StateHolder<State>(initialState: State) {
    private val channel = ConflatedBroadcastChannel(initialState)

    fun get(): State = channel.value

    fun set(state: State) {
        check(channel.offer(state)) {
            "Failed to set state, channel over capacity"
        }
    }

    val flow: Flow<State> =
        channel.asFlow()
            .distinctUntilChanged { old, new -> old === new }

    fun close(cause: Throwable?) {
        channel.close(cause)
    }
}

@ExperimentalCoroutinesApi
private class CommandProcessor<State>(
    bufferSize: Int,
    private val getState: () -> State,
    private val setState: (State) -> Unit,
) : CommandIssuer<State> {
    init {
        require(bufferSize > 0) {
            "bufferSize must be positive. Was: $bufferSize"
        }
    }

    private val channel = Channel<Command<State>>(bufferSize)

    override suspend fun issue(command: Command<State>) = channel.send(command)

    suspend fun process(scope: CoroutineScope) =
        channel.consumeEach { command ->
            val oldState = getState()
            val (newState, actions) = command.reduce(oldState)
            setState(newState)
            for (action in actions) {
                scope.launch { action.execute(this@CommandProcessor) }
            }
        }

    fun close(cause: Throwable?) {
        channel.close(cause)
    }
}

private class StartActionsProcessor<State>(
    actions: Iterable<Action<State>>,
    private val commandIssuer: CommandIssuer<State>,
) {
    private val actions = AtomicReference(actions.toList())

    suspend fun process(scope: CoroutineScope) {
        with(consumeActions()) {
            while (hasNext()) {
                val action = next()
                remove()
                scope.launch { action.execute(commandIssuer) }
            }
        }
    }

    private fun consumeActions(): MutableIterator<Action<State>> {
        val actions = checkNotNull(actions.getAndSet(null))
        return actions.toMutableList().iterator()
    }
}

// TODO more testing required
@ExperimentalCoroutinesApi
private class LiveActionsProcessor<State>(
    actions: Iterable<Action<State>>,
    private val commandIssuer: CommandIssuer<State>,
) {
    private val toggleChannel = Channel<Toggle>(Channel.CONFLATED)

    private var actions: Iterable<Action<State>>? = actions.toList()

    private val flow = toggleChannel.consumeAsFlow()
        .distinctUntilChanged()
        .mapLatest { toggle ->
            when (toggle) {
                Toggle.Enable -> requireNotNull(this.actions)
                Toggle.Disable -> emptyList()
            }
        }

    private val liveCount = AtomicInteger(0)

    suspend fun increaseLiveCount() {
        if (liveCount.incrementAndGet() == 1) {
            toggleChannel.send(Toggle.Enable)
        }
    }

    suspend fun decreaseLiveCount() {
        if (liveCount.decrementAndGet() == 0) {
            toggleChannel.send(Toggle.Disable)
        }
    }

    suspend fun process(scope: CoroutineScope) =
        flow.collectLatest { actions ->
            for (action in actions) {
                scope.launch { action.execute(commandIssuer) }
            }
        }

    fun close(cause: Throwable?) {
        toggleChannel.close(cause)
        actions = null
    }

    private enum class Toggle { Enable, Disable }
}

private const val DEFAULT_BUFFER_SIZE = 64
