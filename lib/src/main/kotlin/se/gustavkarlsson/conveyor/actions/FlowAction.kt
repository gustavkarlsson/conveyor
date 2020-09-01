package se.gustavkarlsson.conveyor.actions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

public class FlowAction<State>(
    private val flow: Flow<Command<State>>
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        flow.collect(issuer::issue)
    }
}
