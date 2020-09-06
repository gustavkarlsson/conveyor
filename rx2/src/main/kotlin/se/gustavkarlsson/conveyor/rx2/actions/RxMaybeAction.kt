package se.gustavkarlsson.conveyor.rx2.actions

import io.reactivex.Maybe
import kotlinx.coroutines.rx2.await
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

public class RxMaybeAction<State : Any>(
    private val maybe: Maybe<Command<State>>,
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        val command = maybe.await()
        if (command != null) {
            issuer.issue(command)
        }
    }
}
