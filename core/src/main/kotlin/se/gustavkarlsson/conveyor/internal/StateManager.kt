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
import se.gustavkarlsson.conveyor.Transformer

@FlowPreview
@ExperimentalCoroutinesApi
internal class StateManager<State>(
    initialState: State,
    private val stateTransformers: Iterable<Transformer<State>>,
) : StateAccess<State>,
    Cancellable {

    private val channel = ConflatedBroadcastChannel<State>()

    private var currentState: State

    init {
        channel.offerOrThrow(initialState)
        currentState = initialState
    }

    override val flow: Flow<State> = channel.asFlow()
        .distinctUntilChanged { old, new -> old === new }
        .onEmpty { emit(get()) }

    override fun get(): State = currentState

    // FIXME synchronize, so that multiple invocations will set the state in sequence
    override suspend fun set(state: State) {
        channel.offerOrThrow(state)
        currentState = state
    }

    // FIXME synchronize, so that multiple invocations will set the state in sequence
    override suspend fun update(block: suspend (State) -> State): State {
        val newState = block(get())
        set(newState)
        return newState
    }

    override fun cancel(cause: Throwable?) {
        channel.cancel(cause as? CancellationException)
    }
}
