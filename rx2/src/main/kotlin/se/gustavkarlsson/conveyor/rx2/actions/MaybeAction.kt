package se.gustavkarlsson.conveyor.rx2.actions

import io.reactivex.Maybe
import kotlinx.coroutines.rx2.await
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

public abstract class MaybeAction<State : Any> : Action<State> {
    final override suspend fun execute(issuer: CommandIssuer<State>) {
        val command = maybe.await()
        if (command != null) {
            issuer.issue(command)
        }
    }

    protected abstract val maybe: Maybe<Command<State>>

    public companion object {
        public operator fun <State : Any> invoke(maybe: Maybe<Command<State>>): MaybeAction<State> =
            ConstructorBlockMaybeAction(maybe)
    }
}

private class ConstructorBlockMaybeAction<State : Any>(
    override val maybe: Maybe<Command<State>>,
) : MaybeAction<State>()
