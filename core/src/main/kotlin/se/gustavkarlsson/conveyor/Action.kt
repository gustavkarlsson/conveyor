package se.gustavkarlsson.conveyor

public interface Action<State> {
    public suspend fun execute(stateAccess: StateAccess<State>)

    public companion object {
        public operator fun <State> invoke(
            block: suspend (stateAccess: StateAccess<State>) -> Unit,
        ): Action<State> = ConstructorAction(block)
    }
}

private class ConstructorAction<State>(
    private val block: suspend (stateAccess: StateAccess<State>) -> Unit,
) : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) = block(stateAccess)
}
