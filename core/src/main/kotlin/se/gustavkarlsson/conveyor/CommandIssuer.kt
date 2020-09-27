package se.gustavkarlsson.conveyor

public interface CommandIssuer<State> {
    public fun issue(command: Command<State>)
}
