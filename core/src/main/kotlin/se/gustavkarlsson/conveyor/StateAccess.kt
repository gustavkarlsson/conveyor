package se.gustavkarlsson.conveyor

public interface StateAccess<State> {
    public val currentState: State
    public fun update(block: (currentState: State) -> State): State
}
