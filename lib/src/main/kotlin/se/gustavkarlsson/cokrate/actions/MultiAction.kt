package se.gustavkarlsson.cokrate.actions

import se.gustavkarlsson.cokrate.Action
import se.gustavkarlsson.cokrate.CommandIssuer

public class MultiAction<State : Any>(
    private val block: suspend CommandIssuer<State>.() -> Unit
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        block(issuer)
    }
}
