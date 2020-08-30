package se.gustavkarlsson.cokrate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
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
    private val initialCommands: Iterable<Command<State>>, // TODO Can we remove this from state?
    bufferSize: Int
) : Store<State> {

    init {
        require(bufferSize > 0) {
            "bufferSize must be positive. Was: $bufferSize"
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

    private val commands = BroadcastChannel<Command<State>>(bufferSize)

    private val states = ConflatedBroadcastChannel(initialState)

    @Synchronized
    override fun start(scope: CoroutineScope): Job {
        val stage = status
        check(stage == Status.NotYetStarted) {
            "Cannot start store when it is $stage"
        }
        val job = scope.launch {
            commands
                .openSubscription()
                .consumeAsFlow()
                .onStart {
                    launch {
                        initialCommands.forEach { commands.send(it) }
                    }
                }
                .collect { command ->
                    val oldState = states.value
                    val (newState, actions) = command.reduce(oldState)
                    states.offer(newState)
                    launch {
                        for (action in actions) {
                            action.execute(commands::send)
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
        val stage = status
        check(stage == Status.Active) { "Cannot issue command while store is $stage" }
        commands.send(command)
    }
}

private enum class Status { NotYetStarted, Active, Cancelled }
