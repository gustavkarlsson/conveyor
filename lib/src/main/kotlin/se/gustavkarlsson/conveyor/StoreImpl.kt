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

    private val commandProcessor = CommandProcessor(commandBufferSize, stateHolder::get, stateHolder::set)

    override val state = stateHolder.flow

    override val currentState get() = stateHolder.get()

    // TODO encapsulate action stuff into new class
    private val initialActions = ArrayDeque(initialActions.toList())

    @Synchronized
    override fun start(scope: CoroutineScope): Job {
        val currentStatus = status
        check(currentStatus == Status.NotYetStarted) {
            "Cannot start store when it is $currentStatus"
        }
        val actionExecutor = ActionExecutor(commandProcessor)
        val job = scope.launch {
            initialActions.removeAll { action ->
                launch { actionExecutor.execute(action) }
                true
            }
            commandProcessor.process {
                launch { actionExecutor.execute(it) }
            }
        }
        job.invokeOnCompletion { stateHolder.close(it) }
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

    private val commands = Channel<Command<State>>(bufferSize)

    override suspend fun issue(command: Command<State>) {
        commands.send(command)
    }

    suspend fun process(onAction: suspend (Action<State>) -> Unit) {
        commands.consumeEach { command ->
            val oldState = getState()
            val (newState, actions) = command.reduce(oldState)
            setState(newState)
            for (action in actions) {
                onAction(action)
            }
        }
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

private class ActionExecutor<State>(
    private val commandIssuer: CommandIssuer<State>,
) {
    suspend fun execute(action: Action<State>) {
        action.execute(commandIssuer)
    }
}
