package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

class NullCommandIssuer<State> : CommandIssuer<State> {
    override fun issue(command: Command<State>) = Unit
}
