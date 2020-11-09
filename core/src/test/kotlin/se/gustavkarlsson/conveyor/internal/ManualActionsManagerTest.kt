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

// TODO Add tests for actions flow
object ManualActionsManagerTest : Spek({
    val scope by memoizedTestCoroutineScope()
    val stateAccess by memoized { SimpleStateAccess(0) }
    val incrementStateAction = IncrementStateAction()

    describe("A ManualActionsManager") {
        val subject by memoized { ManualActionsManager<Int>() }

        describe("that was cancelled") {
            beforeEachTest {
                subject.cancel(null)
            }

            it("throws exception when action is issued") {
                expectThrows<CancellationException> {
                    subject.issue(incrementStateAction)
                }
            }
            it("can be cancelled again") {
                subject.cancel(null)
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
