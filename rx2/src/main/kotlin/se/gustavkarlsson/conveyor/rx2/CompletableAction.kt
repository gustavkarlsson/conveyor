package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Completable
import kotlinx.coroutines.rx2.await
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess

public abstract class CompletableAction<State> : Action<State> {
    final override suspend fun execute(stateAccess: StateAccess<State>): Unit = createCompletable(stateAccess).await()

    protected abstract fun createCompletable(stateAccess: StateAccess<State>): Completable

    public companion object {
        public operator fun <State> invoke(
            createCompletable: (stateAccess: StateAccess<State>) -> Completable
        ): CompletableAction<State> = ConstructorCompletableAction(createCompletable)
    }
}

private class ConstructorCompletableAction<State>(
    private val makeCompletable: (StateAccess<State>) -> Completable
) : CompletableAction<State>() {
    override fun createCompletable(stateAccess: StateAccess<State>): Completable = makeCompletable(stateAccess)
}
