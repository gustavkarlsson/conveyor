package se.gustavkarlsson.conveyor.test

import kotlinx.coroutines.delay
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.internal.ActionProcessor
import strikt.api.Assertion
import strikt.assertions.isEqualTo

class DelayingTrackingActionProcessor<State> : ActionProcessor<State> {
    var completedCount = 0
        private set

    override suspend fun process(stateAccess: UpdatableStateFlow<State>) {
        delay(1)
        completedCount++
    }
}

fun <State> Assertion.Builder<DelayingTrackingActionProcessor<State>>.hasCompletedCount(
    expected: Int,
): Assertion.Builder<DelayingTrackingActionProcessor<State>> =
    with("completedProcesses", { completedCount }) {
        isEqualTo(expected)
    }
