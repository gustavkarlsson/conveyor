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

public fun <State> State.only(): Change<State> = Change(this)

// TODO Consider how this conflicts with other plus operations. Builder DSL?
public operator fun <State> State.plus(action: Action<State>): Change<State> = Change(this, action)

public operator fun <State> Change<State>.plus(action: Action<State>): Change<State> =
    copy(actions = actions + action)
