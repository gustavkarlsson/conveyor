package se.gustavkarlsson.conveyor

/**
 * An operation with access to the state of a [Store]. Actions are the only means of modifying the state
 * and can only be issued while the store is started.
 * Running actions will be cancelled when the store's job is cancelled.
 */
public interface Action<State> {
    /**
     * The function that runs the action. The state can be accessed through the state argument.
     */
    public suspend fun execute(storeFlow: StoreFlow<State>)
}

/**
 * Creates an action with the given block as its execute function.
 */
@Suppress("FunctionName")
public fun <State> Action(
    block: suspend (storeFlow: StoreFlow<State>) -> Unit,
): Action<State> = BasicAction(block)

private class BasicAction<State>(
    private val block: suspend (storeFlow: StoreFlow<State>) -> Unit,
) : Action<State> {
    override suspend fun execute(storeFlow: StoreFlow<State>) = block(storeFlow)
}

internal class UpdateAction<State>(
    private val block: (State) -> State,
) : Action<State> {
    override suspend fun execute(storeFlow: StoreFlow<State>) {
        storeFlow.update(block)
    }
}
