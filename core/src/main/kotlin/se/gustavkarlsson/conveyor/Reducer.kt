package se.gustavkarlsson.conveyor

public interface Reducer<State> {
    public fun reduce(block: (State) -> State): State
}
