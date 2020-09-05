package se.gustavkarlsson.conveyor

public interface CommandIssuer<State> {
    public suspend fun issue(command: Command<State>)
}
