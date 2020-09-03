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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val DEFAULT_BUFFER_SIZE = 64

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State>(
    initialState: State,
    initialActions: Iterable<Action<State>> = emptyList(),
    commandBufferSize: Int = DEFAULT_BUFFER_SIZE,
) : Store<State> {

    private var job: Job? = null

    private val status: Status
        get() = when {
            job == null -> Status.NotYetStarted
            job?.isActive == true -> Status.Active
            else -> Status.Cancelled
        }

    private val stateHolder = StateHolder(initialState)

    override val state = stateHolder.flow

    override val currentState get() = stateHolder.get()

    private val commandProcessor = CommandProcessor(commandBufferSize, stateHolder::get, stateHolder::set)

    private val storeRunner = StoreRunner(initialActions, commandProcessor)

    @Synchronized
    override fun start(scope: CoroutineScope): Job {
        val job = storeRunner.run(scope)
        job.invokeOnCompletion {
            commandProcessor.close(it)
            stateHolder.close(it)
        }
        this.job = job
        return job
    }

    override suspend fun issue(command: Command<State>) {
        val currentStatus = status
        check(currentStatus == Status.Active) {
            "Cannot issue command while store is $currentStatus"
        }
        commandProcessor.issue(command)
    }
}

private enum class Status { NotYetStarted, Active, Cancelled }

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
        channel.send(command)
    }

    suspend fun process(onAction: suspend (Action<State>) -> Unit) {
        channel.consumeEach { command ->
            val oldState = getState()
            val (newState, actions) = command.reduce(oldState)
            setState(newState)
            for (action in actions) {
                onAction(action)
            }
        }
    }

    fun close(cause: Throwable?) {
        channel.close(cause)
    }
}

@FlowPreview
@ExperimentalCoroutinesApi
private class StateHolder<State>(initialState: State) {
    private val channel = ConflatedBroadcastChannel(initialState)

    fun get(): State = channel.value

    fun set(state: State) {
        check(channel.offer(state)) { "Failed to set state, channel over capacity" }
    }

    val flow: Flow<State> = channel.asFlow()
        .distinctUntilChanged { old, new -> old === new }

    fun close(cause: Throwable?) {
        channel.close(cause)
    }
}

@ExperimentalCoroutinesApi
private class StoreRunner<State>(
    initialActions: Iterable<Action<State>>,
    private val commandProcessor: CommandProcessor<State>,
) {
    private var initialActions: MutableIterable<Action<State>>? = ArrayDeque(initialActions.toList())

    @Synchronized
    fun run(scope: CoroutineScope): Job {
        val actions = checkNotNull(initialActions) {
            "Store has already been started"
        }
        return scope.launch {
            for (action in actions) {
                launch { action.execute(commandProcessor) }
            }
            initialActions = null
            commandProcessor.process { action ->
                launch { action.execute(commandProcessor) }
            }
        }
    }
}
