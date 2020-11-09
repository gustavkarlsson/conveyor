package se.gustavkarlsson.conveyor

public interface Action<State> {
    public suspend fun execute(stateAccess: UpdatableStateFlow<State>)
}

public fun <State> action(
    block: suspend (stateAccess: UpdatableStateFlow<State>) -> Unit,
): Action<State> = ConstructorAction(block)

private class ConstructorAction<State>(
    private val block: suspend (stateAccess: UpdatableStateFlow<State>) -> Unit,
) : Action<State> {
    override suspend fun execute(stateAccess: UpdatableStateFlow<State>) = block(stateAccess)
}
