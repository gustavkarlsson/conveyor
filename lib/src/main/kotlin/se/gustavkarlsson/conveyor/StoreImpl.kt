package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State : Any>(
    initialState: State,
    initialCommands: Collection<Command<State>>, // TODO Can we remove this from state?
    commandBufferSize: Int
) : Store<State> {

    private val initialCommands = ArrayDeque(initialCommands)

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

    private val commands = BroadcastChannel<Command<State>>(commandBufferSize)

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
                .openSubscription()
                .consumeAsFlow()
                .onStart {
                    launch {
                        initialCommands.clear {
                            commands.send(it)
                        }
                    }
                }
                .collect { command ->
                    val oldState = states.value
                    val (newState, actions) = command.reduce(oldState)
                    states.offer(newState)
                    launch {
                        for (action in actions) {
                            action.execute(commandIssuer)
                        }
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

private class ChannelCommandIssuer<State : Any>(
    private val channel: SendChannel<Command<State>>
) : CommandIssuer<State> {
    override suspend fun issue(command: Command<State>) {
        channel.send(command)
    }
}

private inline fun <T> MutableCollection<T>.clear(block: (T) -> Unit) {
    val iterator = iterator()
    while (iterator.hasNext()) {
        val item = iterator.next()
        block(item)
        iterator.remove()
    }
}
