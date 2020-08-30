package se.gustavkarlsson.cokrate

public interface Action<Command : Any> {
    public suspend operator fun invoke(emit: suspend (Command) -> Unit)
}
