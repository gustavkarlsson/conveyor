package se.gustavkarlsson.conveyor

public interface StateUpdater<State> {
    public suspend fun update(block: suspend State.() -> State): State
}
