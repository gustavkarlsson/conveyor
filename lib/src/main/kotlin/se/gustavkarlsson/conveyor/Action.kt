package se.gustavkarlsson.conveyor

public interface Action<State> {
    public suspend fun execute(issuer: CommandIssuer<State>)
}
