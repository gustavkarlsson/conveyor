package se.gustavkarlsson.cokrate.actions

import se.gustavkarlsson.cokrate.Action
import se.gustavkarlsson.cokrate.Command
import se.gustavkarlsson.cokrate.CommandIssuer

public class SingleAction<State : Any>(
    private val block: suspend () -> Command<State>
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        val command = block()
        issuer.issue(command)
    }
}
