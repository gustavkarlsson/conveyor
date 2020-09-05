package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

class NullCommandIssuer<State> : CommandIssuer<State> {
    override suspend fun issue(command: Command<State>) = Unit
}
