package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

class TrackingCommandIssuer<T : Any> : CommandIssuer<T> {
    private val _issuedCommands = mutableListOf<Command<T>>()
    val issuedCommands: List<Command<T>> = _issuedCommands

    override suspend fun issue(command: Command<T>) {
        _issuedCommands.add(command)
    }
}
