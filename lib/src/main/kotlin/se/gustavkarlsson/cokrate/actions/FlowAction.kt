package se.gustavkarlsson.cokrate.actions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import se.gustavkarlsson.cokrate.Action
import se.gustavkarlsson.cokrate.Command
import se.gustavkarlsson.cokrate.CommandIssuer

public class FlowAction<State : Any>(
    private val flow: Flow<Command<State>>
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        flow.collect(issuer::issue)
    }
}
