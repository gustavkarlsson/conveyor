package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEmpty
import se.gustavkarlsson.conveyor.Mapper
import se.gustavkarlsson.conveyor.StateAccess

@FlowPreview
@ExperimentalCoroutinesApi
internal class StateManager<State>(
    initialState: State,
    private val stateMappers: Iterable<Mapper<State>>,
) : StateFlowProvider<State>,
    StateAccess<State>,
    Cancellable {

    private val channel = ConflatedBroadcastChannel<State>()

    private var currentState: State

    init {
        channel.offerOrThrow(initialState)
        currentState = initialState
    }

    override val stateFlow: Flow<State> = channel.asFlow()
        .distinctUntilChanged { old, new -> old === new }
        .onEmpty { emit(get()) }

    override fun get(): State = currentState

    override fun set(state: State) {
        val mappedState = stateMappers.fold(state as State?) { acc, mapper ->
            if (acc != null) {
                mapper.map(acc)
            } else {
                acc
            }
        }
        if (mappedState != null) {
            channel.offerOrThrow(mappedState)
            currentState = mappedState
        }
    }

    @Synchronized
    override fun update(block: (State) -> State): State = block(get()).also(::set)

    override fun cancel(cause: Throwable?) {
        channel.cancel(cause as? CancellationException)
    }
}
