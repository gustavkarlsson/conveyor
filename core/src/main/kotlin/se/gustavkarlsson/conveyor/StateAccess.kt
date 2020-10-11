package se.gustavkarlsson.conveyor

public interface StateAccess<State> {
    public fun get(): State
    public fun set(state: State)
    public fun update(block: (currentState: State) -> State): State
}
