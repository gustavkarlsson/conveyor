package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.test.NullAction
import se.gustavkarlsson.conveyor.test.memoizedTestCoroutineScope
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly

object ManualActionsManagerTest : Spek({
    val scope by memoizedTestCoroutineScope()
    val action = NullAction<String>()

    describe("A ManualActionsManager") {
        val subject by memoized { ManualActionsManager<String>() }

        it("suspends on collecting actionFlow") {
            expectSuspends {
                subject.actionFlow.collect()
            }
        }

        describe("that is collecting actionFlow") {
            val collectedActions by memoized {
                mutableListOf<Action<String>>()
            }
            lateinit var collectingJob: Job
            beforeEachTest {
                collectingJob = scope.launch {
                    subject.actionFlow.toCollection(collectedActions)
                }
            }
            afterEachTest {
                collectingJob.cancel("Test ended")
            }

            it("issued action collected") {
                subject.issue(action)
                expectThat(collectedActions).containsExactly(action)
            }
            it("throws if collecting actionFlow again") {
                expectThrows<IllegalStateException> {
                    subject.actionFlow.collect()
                }
            }
        }

        describe("that was cancelled") {
            beforeEachTest {
                subject.cancel()
            }

            it("throws exception when action is issued") {
                expectThrows<CancellationException> {
                    subject.issue(action)
                }
            }
            it("throws exception when collecting actionFlow") {
                expectThrows<CancellationException> {
                    subject.actionFlow.collect()
                }
            }
            it("can be cancelled again") {
                subject.cancel()
            }
        }
    }
})

private fun expectSuspends(block: suspend () -> Unit) {
    expectThrows<TimeoutCancellationException> {
        withTimeout(100) {
            block()
        }
    }
}
