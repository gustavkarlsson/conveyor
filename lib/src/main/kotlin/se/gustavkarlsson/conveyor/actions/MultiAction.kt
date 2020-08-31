package se.gustavkarlsson.conveyor.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.CommandIssuer

public class MultiAction<State : Any>(
    private val block: suspend CommandIssuer<State>.() -> Unit
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        block(issuer)
    }
}
