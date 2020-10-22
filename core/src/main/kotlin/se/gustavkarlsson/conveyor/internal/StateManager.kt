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
    StateAccess<State>,
    Cancellable {
    private val channel = ConflatedBroadcastChannel(initialState)

    override val flow: Flow<State> = channel.asFlow()
        .distinctUntilChanged { old, new -> old === new }
        .onEmpty { emit(get()) }

    private var currentState: State = initialState

    override fun get(): State = currentState

    override fun set(state: State) {
        channel.offerOrThrow(state)
        currentState = state
    }

    @Synchronized
    override fun update(block: (State) -> State): State = block(get()).also(::set)

    override fun cancel(cause: Throwable?) {
        channel.cancel(cause as? CancellationException)
    }
}
