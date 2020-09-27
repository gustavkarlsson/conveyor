package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer
import strikt.api.Assertion
import strikt.assertions.containsExactly

class TrackingCommandIssuer<State> : CommandIssuer<State> {
    private val _issuedCommands = mutableListOf<Command<State>>()
    val issuedCommands: List<Command<State>> = _issuedCommands

    override fun issue(command: Command<State>) {
        _issuedCommands.add(command)
    }
}

fun <State> Assertion.Builder<TrackingCommandIssuer<State>>.hasIssued(
    vararg expected: Command<State>
): Assertion.Builder<TrackingCommandIssuer<State>> =
    with("issuedCommands", { issuedCommands }) {
        containsExactly(*expected)
    }
