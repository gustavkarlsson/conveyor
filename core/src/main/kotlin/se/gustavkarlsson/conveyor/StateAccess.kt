package se.gustavkarlsson.conveyor

public interface StateAccess<State> {
    public fun get(): State
    public suspend fun set(state: State)
    public suspend fun update(block: suspend (currentState: State) -> State): State
}
