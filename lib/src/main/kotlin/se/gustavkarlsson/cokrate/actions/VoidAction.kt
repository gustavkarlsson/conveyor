package se.gustavkarlsson.cokrate.actions

import se.gustavkarlsson.cokrate.Action
import se.gustavkarlsson.cokrate.CommandIssuer

public class VoidAction<State : Any>(
    private val block: suspend () -> Unit
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        block()
    }
}
