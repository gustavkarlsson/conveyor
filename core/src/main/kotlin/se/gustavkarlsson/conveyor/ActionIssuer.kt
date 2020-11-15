package se.gustavkarlsson.conveyor

public interface ActionIssuer<State> {
    public fun issue(action: Action<State>)
}

public fun <State> ActionIssuer<State>.issue(
    block: suspend (state: UpdatableStateFlow<State>) -> Unit,
) {
    issue(Action(block))
}
