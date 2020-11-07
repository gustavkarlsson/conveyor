package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.Flow

public interface StateAccess<State> {
    public val flow: Flow<State>
    public fun get(): State
    public suspend fun set(state: State)
    public suspend fun update(block: suspend State.() -> State): State
}
