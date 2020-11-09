package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.StateFlow

public interface StateAccess<State> : StateFlow<State> {
    public val subscriptionCount: StateFlow<Int>
    public suspend fun update(block: suspend State.() -> State): State
}
