package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.StateFlow

public interface StateAccess<State> {
    public val state: StateFlow<State>
    public suspend fun update(block: suspend State.() -> State): State
}
