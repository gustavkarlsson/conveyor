package se.gustavkarlsson.cokrate

public interface Action<State : Any> {
    public suspend operator fun invoke(issue: suspend (Command<State>) -> Unit)
}
