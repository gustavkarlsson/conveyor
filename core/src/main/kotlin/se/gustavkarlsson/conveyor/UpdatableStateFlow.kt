package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.StateFlow

/**
 * A [StateFlow] that can be updated asynchronously in a predictable way.
 */
public interface UpdatableStateFlow<State> : StateFlow<State> {
    /**
     * Update the state. The provided block will run
     */
    public suspend fun update(block: suspend State.() -> State): State

    /**
     * The number of subscribers of this flow.
     */
    public val subscriptionCount: StateFlow<Int>
}
