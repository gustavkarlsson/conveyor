package se.gustavkarlsson.conveyor.rx2.actions

import io.reactivex.Single
import kotlinx.coroutines.rx2.await
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

public abstract class SingleAction<State : Any> : Action<State> {
    final override suspend fun execute(issuer: CommandIssuer<State>) {
        val command = single.await()
        issuer.issue(command)
    }

    protected abstract val single: Single<Command<State>>

    public companion object {
        public operator fun <State : Any> invoke(single: Single<Command<State>>): SingleAction<State> =
            ConstructorBlockSingleAction(single)
    }
}

private class ConstructorBlockSingleAction<State : Any>(
    override val single: Single<Command<State>>,
) : SingleAction<State>()
