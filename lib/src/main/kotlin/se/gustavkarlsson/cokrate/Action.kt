package se.gustavkarlsson.cokrate

public interface Action<State : Any> {
    public suspend fun execute(issue: suspend (Command<State>) -> Unit)
}
