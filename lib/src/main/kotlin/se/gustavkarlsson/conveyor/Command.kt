package se.gustavkarlsson.conveyor

public fun interface Command<State> {
    public fun reduce(state: State) : Change<State>
}
