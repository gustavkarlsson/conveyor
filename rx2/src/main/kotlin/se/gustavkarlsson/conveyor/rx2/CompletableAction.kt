package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Completable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx2.await
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess
import se.gustavkarlsson.conveyor.rx2.internal.RxStateAccessImpl

@ExperimentalCoroutinesApi
public abstract class CompletableAction<State : Any> : Action<State> {
    final override suspend fun execute(stateAccess: StateAccess<State>) {
        val rxStateAccess = RxStateAccessImpl(stateAccess)
        val completable = createCompletable(rxStateAccess)
        completable.await()
    }

    protected abstract fun createCompletable(stateAccess: RxStateAccess<State>): Completable
}

@ExperimentalCoroutinesApi
public fun <State : Any> completableAction(
    createCompletable: (stateAccess: RxStateAccess<State>) -> Completable,
): CompletableAction<State> = ConstructorCompletableAction(createCompletable)

@ExperimentalCoroutinesApi
private class ConstructorCompletableAction<State : Any>(
    private val makeCompletable: (RxStateAccess<State>) -> Completable,
) : CompletableAction<State>() {
    override fun createCompletable(stateAccess: RxStateAccess<State>): Completable = makeCompletable(stateAccess)
}
