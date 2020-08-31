package se.gustavkarlsson.cokrate

public interface CommandIssuer<State : Any> {
    public suspend fun issue(command: Command<State>)
}
