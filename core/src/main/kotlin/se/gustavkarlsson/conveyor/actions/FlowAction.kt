package se.gustavkarlsson.conveyor.actions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

public abstract class FlowAction<State> : Action<State> {
    final override suspend fun execute(issuer: CommandIssuer<State>): Unit = flow.collect(issuer::issue)

    protected abstract val flow: Flow<Command<State>>

    public companion object {
        public operator fun <State> invoke(flow: Flow<Command<State>>): Action<State> = ConstructorBlockFlowAction(flow)
    }
}

private class ConstructorBlockFlowAction<State>(
    override val flow: Flow<Command<State>>
) : FlowAction<State>()
