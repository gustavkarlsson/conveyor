package se.gustavkarlsson.conveyor.testing

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.channels.Channel
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.internal.ActionIssuer
import strikt.api.Assertion
import strikt.assertions.isEmpty

class TrackingActionIssuer<State> : ActionIssuer<State> {
    private val _issuedActions = mutableListOf<Action<State>>()
    val issuedActions: List<Action<State>> = _issuedActions

    override fun issue(action: Action<State>) {
        _issuedActions.add(action)
        val sendResult = actionsChannel.trySend(action)
        sendResult.getOrThrow()
    }

    private val _cancellations = mutableListOf<Throwable>()
    val cancellations: List<Throwable> = _cancellations

    override fun cancel(cause: Throwable) {
        _cancellations += cause
    }

    private val actionsChannel = Channel<Action<State>>(Channel.BUFFERED)
}

fun <State> TrackingActionIssuer<State>.shouldHaveIssued(vararg expected: Action<State>) {
    withClue("Should have issued $expected") {
        issuedActions.shouldContainExactly(*expected)
    }
}

fun <State> Assertion.Builder<TrackingActionIssuer<State>>.hasNeverBeenCancelled(): Assertion.Builder<TrackingActionIssuer<State>> =
    with("cancellations", { cancellations }) {
        isEmpty()
    }
