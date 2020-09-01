package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer
import strikt.api.Assertion
import strikt.assertions.containsExactly

class TrackingCommandIssuer<T : Any> : CommandIssuer<T> {
    private val _issuedCommands = mutableListOf<Command<T>>()
    val issuedCommands: List<Command<T>> = _issuedCommands

    override suspend fun issue(command: Command<T>) {
        _issuedCommands.add(command)
    }
}

fun <T : Any> Assertion.Builder<TrackingCommandIssuer<T>>.hasIssued(
    vararg expected: Command<T>
): Assertion.Builder<TrackingCommandIssuer<T>> =
    with("issuedCommands", { issuedCommands }) {
        containsExactly(*expected)
    }
