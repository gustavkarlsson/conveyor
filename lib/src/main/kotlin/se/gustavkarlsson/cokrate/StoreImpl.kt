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
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State : Any, Command : Any>(
    initialState: State,
    private val reducer: (State, Command) -> Change<State, Command>,
    private val initialActions: Iterable<Action<Command>>,
    bufferSize: Int
) : Store<State, Command> {

    init {
        require(bufferSize > 0) {
            "bufferSize must be positive. Was: $bufferSize"
        }
    }

    private val commands = BroadcastChannel<Command>(bufferSize)

    private val states = ConflatedBroadcastChannel(initialState)

    override fun start(scope: CoroutineScope): Job =
        scope.launch {
            commands
                .openSubscription()
                .consumeAsFlow()
                .collect { command ->
                    val oldState = states.value
                    val (newState, actions) = reducer(oldState, command)
                    states.offer(newState)
                    for (action in actions) {
                        action(commands::send)
                    }
                }
            for (action in initialActions) {
                action(commands::send)
            }
        }

    override val state = states.asFlow()
        .distinctUntilChanged { old, new -> old === new }

    override suspend fun issue(command: Command) = commands.send(command)
}
