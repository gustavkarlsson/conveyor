package se.gustavkarlsson.cokrate

public fun interface Command<State :Any> {
    public fun reduce(state: State) : Change<State>
}
