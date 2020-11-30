package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import se.gustavkarlsson.conveyor.ActionIssuer
import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException

/**
 * A RxJava version of [Store].
 */
public interface RxStore<State : Any> : ActionIssuer<State> {
    /**
     * The state of the store. Always available regardless of which lifecycle stage the store is in.
     */
    public val state: StateFlowable<State>

    /**
     * The disposable of this store, if it has been started.
     */
    public val disposable: Disposable?

    /**
     * Starts the store in the provided scope.
     * This will run any "start actions" provided and also allows new actions to be issued.
     *
     * A store can only be started once.
     *
     * The store will be stopped when the returned disposable is disposed or its scope is cancelled.
     *
     * *Note: Setting the scope is typically not necessary, but can be useful for unit tests.*
     */
    public fun start(scope: CoroutineScope = GlobalScope): Disposable
}

/**
 * Issues an action to the store with the completable returned by the given block its execute function.
 * The store must be running.
 *
 * Throws [StoreNotYetStartedException] if the store has not yet been started
 * and [StoreStoppedException] if the store has stopped.
 */
@ExperimentalCoroutinesApi
public fun <State : Any> ActionIssuer<State>.issue(
    block: (state: UpdatableStateFlowable<State>) -> Completable,
) {
    issue(CompletableAction(block))
}
