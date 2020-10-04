package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.internal.ActionIssuer
import strikt.api.Assertion
import strikt.assertions.containsExactly

class TrackingActionIssuer<State> : ActionIssuer<State> {
    private val _issuedActions = mutableListOf<Action<State>>()
    val issuedActions: List<Action<State>> = _issuedActions

    override fun issue(action: Action<State>) {
        _issuedActions.add(action)
    }
}

fun <State> Assertion.Builder<TrackingActionIssuer<State>>.hasIssued(
    vararg expected: Action<State>
): Assertion.Builder<TrackingActionIssuer<State>> =
    with("issuedActions", { issuedActions }) {
        containsExactly(*expected)
    }
