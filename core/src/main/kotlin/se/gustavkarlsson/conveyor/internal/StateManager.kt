package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEmpty
import se.gustavkarlsson.conveyor.StateAccess

@FlowPreview
@ExperimentalCoroutinesApi
internal class StateManager<State>(initialState: State) :
    StateFlowProvider<State>,
    StateAccess<State>,
    Cancellable {
    private val channel = ConflatedBroadcastChannel(initialState)

    override val stateFlow: Flow<State> = channel.asFlow()
        .distinctUntilChanged { old, new -> old === new }
        .onEmpty { emit(currentState) }

    override var currentState: State = initialState
        private set(value) {
            channel.offerOrThrow(value)
            field = value
        }

    @Synchronized
    override fun update(block: (State) -> State): State {
        val newState = block(currentState)
        currentState = newState
        return newState
    }

    override fun cancel(cause: Throwable?) {
        channel.cancel(cause as? CancellationException)
    }
}
