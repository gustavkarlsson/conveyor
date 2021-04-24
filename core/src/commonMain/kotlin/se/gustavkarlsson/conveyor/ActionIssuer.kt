package se.gustavkarlsson.conveyor

/**
 * Enables updating the state of a [Store] by issuing actions to it.
 */
public interface ActionIssuer<State> {
    /**
     * Issues the given action to the store. The store must be running.
     *
     * Throws [StoreNotYetStartedException] if the store has not yet been started
     * and [StoreStoppedException] if the store has stopped.
     */
    public fun issue(action: Action<State>)
}

/**
 * Issues an action to the store with the given block its execute function. The store must be running.
 *
 * Throws [StoreNotYetStartedException] if the store has not yet been started
 * and [StoreStoppedException] if the store has stopped.
 */
public fun <State> ActionIssuer<State>.issue(
    block: suspend (stateFlow: AtomicStateFlow<State>) -> Unit,
) {
    issue(Action(block))
}
