package se.gustavkarlsson.conveyor.rx2.actions

import io.reactivex.Completable
import kotlinx.coroutines.rx2.await
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.CommandIssuer

public class RxCompletableAction<State : Any>(
    private val completable: Completable,
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        completable.await()
    }
}
