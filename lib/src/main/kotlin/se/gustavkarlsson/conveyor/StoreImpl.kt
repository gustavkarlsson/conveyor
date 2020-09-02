package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
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

    // TODO encapsulate command stuff into new class
    private val commands = Channel<Command<State>>(commandBufferSize)

    private val commandIssuer = ChannelCommandIssuer(commands)

    // TODO encapsulate state stuff into new class
    private val states = ConflatedBroadcastChannel(initialState)

    private val stateSetter = StateSetter(states)

    override val state = states.asFlow()
        .distinctUntilChanged { old, new -> old === new }

    override val currentState: State get() = states.value

    // TODO encapsulate action stuff into new class
    private val initialActions = ArrayDeque(initialActions.toList())

    private val actionExecutor = ActionExecutor(commandIssuer)

    init {
        require(commandBufferSize > 0) {
            "commandBufferSize must be positive. Was: $commandBufferSize"
        }
        stateSetter.set(initialState)
    }

    @Synchronized
    override fun start(scope: CoroutineScope): Job {
        val currentStatus = status
        check(currentStatus == Status.NotYetStarted) {
            "Cannot start store when it is $currentStatus"
        }
        val job = scope.launch {
            initialActions.removeAll { action ->
                launch { actionExecutor.execute(action) }
                true
            }
            commands
                .consumeEach { command ->
                    val oldState = states.value
                    val (newState, actions) = command.reduce(oldState)
                    stateSetter.set(newState)
                    for (action in actions) {
                        launch { actionExecutor.execute(action) }
                    }
                }
        }
        job.invokeOnCompletion { states.close(it) }
        this.job = job
        return job
    }

    override suspend fun issue(command: Command<State>) {
        val currentStatus = status
        check(currentStatus == Status.Active) {
            "Cannot issue command while store is $currentStatus"
        }
        commandIssuer.issue(command)
    }
}

private enum class Status { NotYetStarted, Active, Cancelled }

private class ChannelCommandIssuer<State>(
    private val channel: SendChannel<Command<State>>,
) : CommandIssuer<State> {
    override suspend fun issue(command: Command<State>) {
        channel.send(command)
    }
}

private class StateSetter<State>(
    private val channel: SendChannel<State>,
) {
    fun set(state: State) {
        check(channel.offer(state)) { "Failed to set state, channel over capacity" }
    }
}

private class ActionExecutor<State>(
    private val commandIssuer: CommandIssuer<State>,
) {
    suspend fun execute(action: Action<State>) {
        action.execute(commandIssuer)
    }
}
