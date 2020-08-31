package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

class TrackingCommandIssuer<T : Any> : CommandIssuer<T> {
    val issuedCommands = mutableListOf<Command<T>>()

    override suspend fun issue(command: Command<T>) {
        issuedCommands.add(command)
    }
}
