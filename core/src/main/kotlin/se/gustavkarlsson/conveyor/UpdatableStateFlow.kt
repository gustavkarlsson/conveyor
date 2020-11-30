package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A [StateFlow] that can be updated sequentially, guaranteeing predictable state changes.
 */
public interface UpdatableStateFlow<State> : StateFlow<State> {
    /**
     * Updates the state using the given block, returning the new state.
     * The receiver argument of the block is the current state at the time the block runs.
     *
     * State updates run sequentially, which guarantees that the state does not change while an update block runs.
     * This function will therefore suspend if another update is in progress,
     * even if the provided block does not suspend.
     */
    public suspend fun update(block: suspend State.() -> State): State

    // FIXME rename?
    /**
     * The number of subscriber of the **store**
     * Acts like [MutableSharedFlow.subscriptionCount] but for the external state flow of the store.
     */
    public val storeSubscriberCount: StateFlow<Int>
}
