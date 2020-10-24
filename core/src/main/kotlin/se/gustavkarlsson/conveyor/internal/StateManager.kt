package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import se.gustavkarlsson.conveyor.StateAccess

@FlowPreview
@ExperimentalCoroutinesApi
internal class StateManager<State>(
    initialState: State,
) : StateAccess<State>, Cancellable {
    // TODO can we NOT use a separate internalState?
    private var internalState: State = initialState
    private val stateChannel = ConflatedBroadcastChannel<State>()
        .apply { offerOrThrow(initialState) }

    override val flow: Flow<State> = stateChannel.asFlow()
        .onEmpty { emit(internalState) }

    override fun get(): State = internalState

    private val writeMutex = Mutex()

    override suspend fun set(state: State) {
        writeMutex.withLock {
            setIfDifferent(state)
        }
    }

    override suspend fun update(block: suspend (State) -> State) {
        writeMutex.withLock {
            val state = block(internalState)
            setIfDifferent(state)
        }
    }

    private fun setIfDifferent(state: State) {
        if (internalState !== state) {
            stateChannel.offerOrThrow(state)
            internalState = state
        }
    }

    override fun cancel(cause: Throwable?) {
        stateChannel.cancel(cause as? CancellationException)
    }
}
