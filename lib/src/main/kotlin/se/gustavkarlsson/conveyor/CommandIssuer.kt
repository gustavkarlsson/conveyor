package se.gustavkarlsson.conveyor

public interface CommandIssuer<State : Any> {
    public suspend fun issue(command: Command<State>)
}
