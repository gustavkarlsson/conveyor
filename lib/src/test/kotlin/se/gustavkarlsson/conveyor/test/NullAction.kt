package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.CommandIssuer

class NullAction<T : Any> : Action<T> {
    override suspend fun execute(issuer: CommandIssuer<T>) = Unit
}
