package se.gustavkarlsson.conveyor.testing

import kotlinx.coroutines.channels.Channel
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.internal.ActionIssuer
import strikt.api.Assertion
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

class TrackingActionIssuer<State> : ActionIssuer<State> {
    private val _issuedActions = mutableListOf<Action<State>>()
    val issuedActions: List<Action<State>> = _issuedActions

    override fun issue(action: Action<State>) {
        _issuedActions.add(action)
        require(actionsChannel.offer(action)) { "Offer failed" }
    }

    private val _cancellations = mutableListOf<Throwable?>()
    val cancellations: List<Throwable?> = _cancellations

    override fun cancel(cause: Throwable?) {
        _cancellations += cause
    }

    private val actionsChannel = Channel<Action<State>>(Channel.BUFFERED)
}

fun <State> Assertion.Builder<TrackingActionIssuer<State>>.hasIssued(
    vararg expected: Action<State>
): Assertion.Builder<TrackingActionIssuer<State>> =
    with("issuedActions", { issuedActions }) {
        containsExactly(*expected)
    }

fun <State> Assertion.Builder<TrackingActionIssuer<State>>.hasBeenCancelledWith(
    vararg expected: Throwable?,
): Assertion.Builder<TrackingActionIssuer<State>> =
    with("cancellations", { cancellations }) {
        containsExactly(*expected)
    }

fun <State> Assertion.Builder<TrackingActionIssuer<State>>.hasNeverBeenCancelled(
): Assertion.Builder<TrackingActionIssuer<State>> =
    with("cancellations", { cancellations }) {
        isEmpty()
    }
