package se.gustavkarlsson.conveyor.actions

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.CommandIssuer

public class VoidAction<State : Any>(
    private val block: suspend () -> Unit
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        block()
    }
}
