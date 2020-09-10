package se.gustavkarlsson.conveyor.rx2.actions

import io.reactivex.Completable
import kotlinx.coroutines.rx2.await
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.CommandIssuer

public abstract class CompletableAction<State : Any> : Action<State> {
    final override suspend fun execute(issuer: CommandIssuer<State>): Unit = completable.await()

    protected abstract val completable: Completable

    public companion object {
        public operator fun <State : Any> invoke(completable: Completable): Action<State> =
            ConstructorBlockCompletableAction(completable)
    }
}

private class ConstructorBlockCompletableAction<State : Any>(
    override val completable: Completable,
) : CompletableAction<State>()
