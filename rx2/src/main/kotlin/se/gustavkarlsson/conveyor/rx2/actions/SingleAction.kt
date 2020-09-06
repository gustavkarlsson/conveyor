package se.gustavkarlsson.conveyor.rx2.actions

import io.reactivex.Single
import kotlinx.coroutines.rx2.await
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

public class SingleAction<State : Any>(
    private val single: Single<Command<State>>,
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        val command = single.await()
        issuer.issue(command)
    }
}
