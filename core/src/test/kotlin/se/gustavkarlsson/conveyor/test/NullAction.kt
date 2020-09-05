package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.CommandIssuer

class NullAction<State> : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) = Unit
}
