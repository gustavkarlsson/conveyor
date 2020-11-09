package se.gustavkarlsson.conveyor

public interface Action<State> {
    public suspend fun execute(state: UpdatableStateFlow<State>)
}

public fun <State> action(
    block: suspend (state: UpdatableStateFlow<State>) -> Unit,
): Action<State> = ConstructorAction(block)

private class ConstructorAction<State>(
    private val block: suspend (state: UpdatableStateFlow<State>) -> Unit,
) : Action<State> {
    override suspend fun execute(state: UpdatableStateFlow<State>) = block(state)
}
