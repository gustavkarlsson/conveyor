package se.gustavkarlsson.conveyor.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

public class SingleAction<State>(
    private val block: suspend () -> Command<State>
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        val command = block()
        issuer.issue(command)
    }
}
