package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.action
import se.gustavkarlsson.conveyor.test.IncrementStateAction
import se.gustavkarlsson.conveyor.test.SimpleStateAccess
import se.gustavkarlsson.conveyor.test.memoizedTestCoroutineScope
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

object ManualActionsManagerTest : Spek({
    val scope by memoizedTestCoroutineScope()
    val stateAccess by memoized { SimpleStateAccess(0) }
    val incrementStateAction = IncrementStateAction()

    describe("A ManualActionsManager") {
        val subject by memoized { ManualActionsManager<Int>() }

        it("suspends while processing") {
            expectSuspends {
                subject.process(stateAccess)
            }
        }

        describe("that is processing") {
            lateinit var processingJob: Job
            beforeEachTest {
                processingJob = scope.launch {
                    subject.process(stateAccess)
                }
            }
            afterEachTest {
                processingJob.cancel("Test ended")
            }

            it("executes issued actions in parallel") {
                val delayAction = action<Int> { access ->
                    delay(1)
                    access.update { this + 1 }
                }
                subject.issue(delayAction)
                subject.issue(delayAction)
                scope.advanceTimeBy(1)
                expectThat(stateAccess.get()).isEqualTo(2)
            }
            it("throws if processing again") {
                expectThrows<IllegalStateException> {
                    subject.process(stateAccess)
                }
            }
        }

        describe("that was cancelled") {
            beforeEachTest {
                subject.cancel()
            }

            it("throws exception when action is issued") {
                expectThrows<CancellationException> {
                    subject.issue(incrementStateAction)
                }
            }
            it("throws exception when processing") {
                expectThrows<CancellationException> {
                    subject.process(stateAccess)
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
