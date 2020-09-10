package se.gustavkarlsson.conveyor.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

public abstract class SingleAction<State> : Action<State> {
    final override suspend fun execute(issuer: CommandIssuer<State>): Unit = issuer.issue(execute())

    protected abstract suspend fun execute(): Command<State>

    public companion object {
        public operator fun <State> invoke(block: suspend () -> Command<State>): Action<State> =
            ConstructorBlockSingleAction(block)
    }
}

private class ConstructorBlockSingleAction<State>(
    private val block: suspend () -> Command<State>
) : SingleAction<State>() {
    override suspend fun execute() = block()
}
