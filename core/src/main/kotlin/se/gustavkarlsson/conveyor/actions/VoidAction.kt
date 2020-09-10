package se.gustavkarlsson.conveyor.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.CommandIssuer

public abstract class VoidAction<State> : Action<State> {
    final override suspend fun execute(issuer: CommandIssuer<State>): Unit = execute()

    protected abstract suspend fun execute()

    public companion object {
        public operator fun <State> invoke(block: suspend () -> Unit): Action<State> {
            return ConstructorBlockVoidAction(block)
        }
    }
}

private class ConstructorBlockVoidAction<State>(
    private val block: suspend () -> Unit
) : VoidAction<State>() {
    override suspend fun execute() = block()
}
