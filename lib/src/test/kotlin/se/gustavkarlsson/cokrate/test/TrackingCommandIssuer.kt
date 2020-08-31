package se.gustavkarlsson.cokrate.test

import se.gustavkarlsson.cokrate.Command
import se.gustavkarlsson.cokrate.CommandIssuer

class TrackingCommandIssuer<T : Any> : CommandIssuer<T> {
    val issuedCommands = mutableListOf<Command<T>>()

    override suspend fun issue(command: Command<T>) {
        issuedCommands.add(command)
    }
}
