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

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State>(
    initialState: State,
    initialActions: List<Action<State>>,
    commandBufferSize: Int,
) : Store<State> {

    private var job: Job? = null

    private val status: Status
        get() = when {
            job == null -> Status.NotYetStarted
            job?.isActive == true -> Status.Active
            else -> Status.Cancelled
        }

    private val commands = Channel<Command<State>>(commandBufferSize)

    private val states = ConflatedBroadcastChannel(initialState)

    override val state = states.asFlow()
        .distinctUntilChanged { old, new -> old === new }

    override val currentState: State get() = states.value

    private val initialActions = ArrayDeque(initialActions)

    init {
        require(commandBufferSize > 0) {
            "commandBufferSize must be positive. Was: $commandBufferSize"
        }
    }

    @Synchronized
    override fun start(scope: CoroutineScope): Job {
        val currentStatus = status
        check(currentStatus == Status.NotYetStarted) {
            "Cannot start store when it is $currentStatus"
        }
        val job = scope.launch {
            val commandIssuer = ChannelCommandIssuer(commands)
            initialActions.removeAll { action ->
                launch { action.execute(commandIssuer) }
                true
            }
            commands
                .consumeEach { command ->
                    val oldState = states.value
                    val (newState, actions) = command.reduce(oldState)
                    states.offer(newState)
                    for (action in actions) {
                        launch { action.execute(commandIssuer) }
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
        commands.send(command)
    }
}

private enum class Status { NotYetStarted, Active, Cancelled }

private class ChannelCommandIssuer<State>(
    private val channel: SendChannel<Command<State>>
) : CommandIssuer<State> {
    override suspend fun issue(command: Command<State>) {
        channel.send(command)
    }
}
