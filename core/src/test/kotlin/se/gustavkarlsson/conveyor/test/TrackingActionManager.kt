package se.gustavkarlsson.conveyor.test

import kotlinx.coroutines.delay
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.internal.ActionManager
import strikt.api.Assertion
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo

class TrackingActionManager<State> : ActionManager<State> {
    private val _issuedActions = mutableListOf<Action<State>>()
    val issuedActions: List<Action<State>> = _issuedActions

    override fun issue(action: Action<State>) {
        _issuedActions.add(action)
    }

    private val _cancellations = mutableListOf<Throwable?>()
    val cancellations: List<Throwable?> = _cancellations

    override fun cancel(cause: Throwable?) {
        _cancellations += cause
    }

    var processes = 0
        private set

    override suspend fun process(stateAccess: UpdatableStateFlow<State>) {
        delay(1)
        processes++
    }
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

fun <State> Assertion.Builder<TrackingActionManager<State>>.hasCompletedCount(
    expected: Int,
): Assertion.Builder<TrackingActionManager<State>> =
    with("processes", { processes }) {
        isEqualTo(expected)
    }
