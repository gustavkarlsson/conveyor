package se.gustavkarlsson.conveyor.testing

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.internal.ActionManager
import strikt.api.Assertion
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

class TrackingActionManager<State> : ActionManager<State> {
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

    private val actionsChannel = Channel<Action<State>>()
    override val actions: Flow<Action<State>> = actionsChannel.consumeAsFlow()
}

fun <State> Assertion.Builder<TrackingActionManager<State>>.hasIssued(
    vararg expected: Action<State>
): Assertion.Builder<TrackingActionManager<State>> =
    with("issuedActions", { issuedActions }) {
        containsExactly(*expected)
    }

fun <State> Assertion.Builder<TrackingActionManager<State>>.hasBeenCancelledWith(
    vararg expected: Throwable?,
): Assertion.Builder<TrackingActionManager<State>> =
    with("cancellations", { cancellations }) {
        containsExactly(*expected)
    }

fun <State> Assertion.Builder<TrackingActionManager<State>>.hasNeverBeenCancelled(
): Assertion.Builder<TrackingActionManager<State>> =
    with("cancellations", { cancellations }) {
        isEmpty()
    }
