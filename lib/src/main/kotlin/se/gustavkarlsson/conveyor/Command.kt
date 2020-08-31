package se.gustavkarlsson.conveyor

public fun interface Command<State :Any> {
    public fun reduce(state: State) : Change<State>
}
