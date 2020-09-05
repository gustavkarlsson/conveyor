package se.gustavkarlsson.conveyor

// TODO Remove Suppress once fixed: https://github.com/detekt/detekt/issues/3050
@Suppress("ModifierOrder")
public fun interface Command<State> {
    public fun reduce(state: State) : Change<State>
}
