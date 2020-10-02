package se.gustavkarlsson.conveyor

public interface Action<State> {
    public suspend fun execute(updateState: UpdateState<State>)

    public companion object {
        public operator fun <State> invoke(block: suspend (updateState: UpdateState<State>) -> Unit): Action<State> =
            ConstructorAction(block)
    }
}

private class ConstructorAction<State>(
    private val block: suspend (updateState: UpdateState<State>) -> Unit
) : Action<State> {
    override suspend fun execute(updateState: UpdateState<State>) = block(updateState)
}
