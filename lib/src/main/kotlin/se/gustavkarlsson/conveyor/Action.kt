package se.gustavkarlsson.conveyor

public interface Action<State : Any> {
    public suspend fun execute(issuer: CommandIssuer<State>)
}
