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
    public suspend fun execute(state: UpdatableStateFlow<State>)
}

/**
 * Creates an action with the given block as its execute function.
 */
@Suppress("FunctionName")
public fun <State> Action(
    block: suspend (state: UpdatableStateFlow<State>) -> Unit,
): Action<State> = ConstructorAction(block)

private class ConstructorAction<State>(
    private val block: suspend (state: UpdatableStateFlow<State>) -> Unit,
) : Action<State> {
    override suspend fun execute(state: UpdatableStateFlow<State>) = block(state)
}
