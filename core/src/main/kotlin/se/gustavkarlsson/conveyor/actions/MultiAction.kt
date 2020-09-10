package se.gustavkarlsson.conveyor.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.CommandIssuer

public abstract class MultiAction<State> : Action<State> {
    public companion object {
        public operator fun <State> invoke(block: suspend CommandIssuer<State>.() -> Unit): Action<State> =
            ConstructorBlockMultiAction(block)
    }
}

private class ConstructorBlockMultiAction<State>(
    private val block: suspend CommandIssuer<State>.() -> Unit,
) : MultiAction<State>() {
    override suspend fun execute(issuer: CommandIssuer<State>) = issuer.block()
}
