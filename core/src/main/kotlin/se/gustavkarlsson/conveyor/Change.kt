package se.gustavkarlsson.conveyor

public data class Change<State>(
    val newState: State,
    val actions: List<Action<State>>,
) {
    public constructor(
        newState: State,
        vararg actions: Action<State>,
    ) : this(newState, actions.asList())
}
