package se.gustavkarlsson.conveyor

public interface ActionIssuer<State> {
    public fun issue(action: Action<State>)
}
