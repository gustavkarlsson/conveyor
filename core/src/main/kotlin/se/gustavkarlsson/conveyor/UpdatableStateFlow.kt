package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.StateFlow

public interface UpdatableStateFlow<State> : StateFlow<State> {
    public suspend fun update(block: suspend State.() -> State): State
    public val subscriptionCount: StateFlow<Int>
}
