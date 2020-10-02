package se.gustavkarlsson.conveyor

public interface UpdateState<State> {
    public operator fun invoke(block: (currentState: State) -> State): State
}
