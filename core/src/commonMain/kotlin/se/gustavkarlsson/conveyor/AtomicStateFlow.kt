package se.gustavkarlsson.conveyor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A [StateFlow] that can be updated atomically, guaranteeing predictable state changes.
 */
public interface AtomicStateFlow<State> : StateFlow<State>, MutableSharedFlow<State> { // FIXME Rename?
    /**
     * Updates the state using the given block.
     * The argument of the block is the current state at the time the block runs.
     *
     * Updates run in sequence, which guarantees that the state does not change while the update runs.
     * This function will suspend if another update is running, or due to back pressure.
     */
    public suspend fun update(block: (State) -> State)

    /**
     * Updates the state using the given block, returning the new state.
     * The argument of the block is the current state at the time the block runs.
     *
     * Updates run in sequence, which guarantees that the state does not change while the update runs.
     * This function will suspend if another update is running, or due to back pressure.
     */
    public suspend fun updateAndGet(block: (State) -> State): State

    /**
     * Updates the state using the given block, returning the previous state.
     * The argument of the block is the current state at the time the block runs.
     *
     * Updates run in sequence, which guarantees that the state does not change while the update runs.
     * This function will suspend if another update is running, or due to back pressure.
     */
    public suspend fun getAndUpdate(block: (State) -> State): State

    /**
     * The number of subscriber of the **store**
     * Acts like [MutableSharedFlow.subscriptionCount] but for the external state flow of the store.
     */
    public val storeSubscriberCount: StateFlow<Int>

    @ExperimentalCoroutinesApi
    @Deprecated("Not supported", level = DeprecationLevel.HIDDEN)
    override fun resetReplayCache() {
        throw UnsupportedOperationException("${this::class.simpleName}.resetReplayCache is not supported")
    }
}
