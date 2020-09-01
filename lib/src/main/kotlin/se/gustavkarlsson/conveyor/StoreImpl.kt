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
    initialCommands: Collection<Command<State>>,
    commandBufferSize: Int,
) : Store<State> {

    init {
        require(commandBufferSize > 0) {
            "commandBufferSize must be positive. Was: $commandBufferSize"
        }
    }

    private var job: Job? = null
        set(value) {
            check(field == null)
            checkNotNull(value)
            field = value
        }

    private val status: Status
        get() = when {
            job == null -> Status.NotYetStarted
            job?.isActive == true -> Status.Active
            else -> Status.Cancelled
        }

    private val commands = Channel<Command<State>>(commandBufferSize)
        .also { channel ->
            for (command in initialCommands) {
                require(channel.offer(command)) {
                    "Initial command count is greater than command buffer size"
                }
            }
        }

    private val states = ConflatedBroadcastChannel(initialState)

    @Synchronized
    override fun start(scope: CoroutineScope): Job {
        val currentStatus = status
        check(currentStatus == Status.NotYetStarted) {
            "Cannot start store when it is $currentStatus"
        }
        val job = scope.launch {
            val commandIssuer = ChannelCommandIssuer(commands)
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
        this.job = job
        return job
    }

    override val state = states.asFlow()
        .distinctUntilChanged { old, new -> old === new }

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
