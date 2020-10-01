package se.gustavkarlsson.conveyor

public interface Action<State> {
    public suspend fun execute(reducer: Reducer<State>) // FIXME Turn this into a function?

    // TODO Keep this? Will it be used?
    public companion object {
        public operator fun <State> invoke(
            block: suspend Reducer<State>.() -> Unit
        ): Action<State> = ConstructorAction(block)
    }
}

private class ConstructorAction<State>(
    private val block: suspend Reducer<State>.() -> Unit
) : Action<State> {
    override suspend fun execute(reducer: Reducer<State>) = reducer.block()
}
