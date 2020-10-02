package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Completable
import kotlinx.coroutines.rx2.await
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdateState

public abstract class CompletableAction<State> : Action<State> {
    final override suspend fun execute(updateState: UpdateState<State>): Unit = createCompletable(updateState).await()

    protected abstract fun createCompletable(updateState: UpdateState<State>): Completable

    public companion object {
        public operator fun <State> invoke(
            createCompletable: (updateState: UpdateState<State>) -> Completable
        ): CompletableAction<State> = ConstructorCompletableAction(createCompletable)
    }
}

private class ConstructorCompletableAction<State>(
    private val makeCompletable: (UpdateState<State>) -> Completable
) : CompletableAction<State>() {
    override fun createCompletable(updateState: UpdateState<State>): Completable = makeCompletable(updateState)
}
