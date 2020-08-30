package se.gustavkarlsson.cokrate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State : Any>(
    initialState: State,
    private val initialCommands: Iterable<Command<State>>,
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

    private val stage: Stage
        get() = when {
            job == null -> Stage.NotYetStarted
            job?.isActive == true -> Stage.Active
            else -> Stage.Stopped
        }

    private val commands = BroadcastChannel<Command<State>>(bufferSize)

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
                        initialCommands.forEach(commands::offer)
                    }
                }
                .collect { command ->
                    val oldState = states.value
                    val (newState, actions) = command.reduce(oldState)
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

    override suspend fun issue(command: Command<State>) {
        val stage = stage
        check(stage == Stage.Active) { "Cannot issue command while store is $stage" }
        commands.send(command)
    }
}

private enum class Stage { NotYetStarted, Active, Stopped }

public fun main() {
    val store = StoreImpl(
        initialState = 0,
        initialCommands = listOf(object : Command<Int> {
            override fun reduce(state: Int): Change<Int> {
                return (state + 1).only()
            }
        }),
        bufferSize = 64
    )

    runBlocking {

        launch {
            store.state.collect {
                println(it)
            }
        }
        store.start(GlobalScope)
    }
}
