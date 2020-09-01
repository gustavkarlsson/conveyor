package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

class NullCommandIssuer<T : Any> : CommandIssuer<T> {
    override suspend fun issue(command: Command<T>) = Unit
}
