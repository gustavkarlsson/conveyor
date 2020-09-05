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

private const val DEFAULT_BUFFER_SIZE = 64

// TODO Add support for cold sources? https://www.halfbit.de/posts/external-events-in-knot/
// TODO Add support for watchers?
// TODO Add support for side effects/events/interceptors
// TODO React to state/changes?

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State>(
    initialState: State,
    initialActions: Iterable<Action<State>> = emptyList(),
    onlineActions: Iterable<Action<State>> = emptyList(), // TODO clear somehow after cancellation
    commandBufferSize: Int = DEFAULT_BUFFER_SIZE,
) : Store<State> {

    private val stateHolder = StateHolder(initialState)

    // TODO Extract into own class
    private val onlineCount = AtomicInteger(0)
    private val onlineActionSignals = Channel<Boolean>(Channel.CONFLATED) // TODO use Enum instead
    private val onlineActionsFlow = onlineActionSignals.consumeAsFlow()
        .distinctUntilChanged()
        .mapLatest { active ->
            if (active) {
                onlineActions
            } else {
                emptyList()
            }
        }

    override val state = stateHolder.flow
        .onStart {
            if (onlineCount.incrementAndGet() == 1) {
                onlineActionSignals.send(true)
            }
        }
        .onCompletion {
            if (onlineCount.decrementAndGet() == 0) {
                onlineActionSignals.send(false)
            }
        }

    override val currentState get() = stateHolder.get()

    private val commandProcessor = CommandProcessor(commandBufferSize, stateHolder::get, stateHolder::set)

    private val initialActions = AtomicReference(initialActions)

    override fun start(scope: CoroutineScope): Job {
        val job = scope.launch {
            launch {
                onlineActionsFlow.collectLatest { actions ->
                    for (action in actions) {
                        launch { action.execute(commandProcessor) }
                    }
                }
            }
            takeInitialActions().removeAll { action ->
                launch { action.execute(commandProcessor) }
                true
            }
            commandProcessor.process { action ->
                launch { action.execute(commandProcessor) }
            }
        }
        job.invokeOnCompletion { throwable ->
            onlineActionSignals.close(throwable)
            commandProcessor.close(throwable)
            stateHolder.close(throwable)
        }
        return job
    }

    private fun takeInitialActions(): MutableIterable<Action<State>> {
        val actions = checkNotNull(initialActions.getAndSet(null)) {
            "Store has already been started"
        }
        return ArrayDeque(actions.toList())
    }

    override suspend fun issue(command: Command<State>) = commandProcessor.issue(command)
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

    override suspend fun issue(command: Command<State>) {
        check(!channel.isClosedForSend) { "Store has been stopped" }
        channel.send(command)
    }

    suspend fun process(onAction: suspend (Action<State>) -> Unit) =
        channel.consumeEach { command ->
            val oldState = getState()
            val (newState, actions) = command.reduce(oldState)
            setState(newState)
            for (action in actions) {
                onAction(action)
            }
        }

    fun close(cause: Throwable?) {
        channel.close(cause)
    }
}
