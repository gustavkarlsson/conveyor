package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.Flow

public interface StateAccess<State> {
    public val flow: Flow<State>
    public fun get(): State
    public fun set(state: State)
    public fun update(block: (currentState: State) -> State): State
}
