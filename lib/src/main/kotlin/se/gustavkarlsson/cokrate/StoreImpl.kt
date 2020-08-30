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
internal class StoreImpl<State : Any, Command : Any>(
    initialState: State,
    private val reducer: Reducer<State, Command>,
    private val initialActions: Iterable<Action<Command>>,
    bufferSize: Int
) : Store<State, Command> {

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

    // TODO rename to avoid similarity with state?
    override val stage: Stage
        get() = when {
            job == null -> Stage.NotYetStarted
            job?.isActive == true -> Stage.Active
            else -> Stage.Stopped
        }

    private val commands = BroadcastChannel<Command>(bufferSize)

    private val states = ConflatedBroadcastChannel(initialState)

    @Synchronized
    override fun start(scope: CoroutineScope): Job {
        val stage = stage
        check(stage == Stage.NotYetStarted) {
            "Cannot start store when it is $stage"
        }
        val job = scope.launch {
            commands
                .openSubscription()
                .consumeAsFlow()
                .onStart {
                    launch {
                        for (action in initialActions) {
                            action(commands::send)
                        }
                    }
                }
                .collect { command ->
                    val oldState = states.value
                    val (newState, actions) = reducer(oldState, command)
                    states.offer(newState)
                    launch {
                        for (action in actions) {
                            action(commands::send)
                        }
                    }
                }
        }
        this.job = job
        return job
    }

    override val state = states.asFlow()
        .distinctUntilChanged { old, new -> old === new }

    override suspend fun issue(command: Command) {
        val stage = stage
        check(stage == Stage.Active) { "Cannot issue command while store is $stage" }
        commands.send(command)
    }
}

public typealias Reducer<State, Command> = (state: State, command: Command) -> Change<State, Command>

public enum class Stage { NotYetStarted, Active, Stopped }
