package se.gustavkarlsson.conveyor.test

import se.gustavkarlsson.conveyor.internal.Cancellable
import strikt.api.Assertion
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

class TrackingCancellable : Cancellable {
    private val _cancellations = mutableListOf<Throwable?>()
    val cancellations: List<Throwable?> = _cancellations

    override fun cancel(cause: Throwable?) {
        _cancellations += cause
    }
}

fun Assertion.Builder<TrackingCancellable>.hasBeenCancelledWith(
    vararg expected: Throwable?,
): Assertion.Builder<TrackingCancellable> =
    with("cancellations", { cancellations }) {
        containsExactly(*expected)
    }

fun Assertion.Builder<TrackingCancellable>.hasNeverBeenCancelled(): Assertion.Builder<TrackingCancellable> =
    with("cancellations", { cancellations }) {
        isEmpty()
    }
