package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.IncrementStateAction
import se.gustavkarlsson.conveyor.test.memoizedTestCoroutineScope
import strikt.api.expectThrows

// TODO Add tests for actions flow
object ActionManagerImplTest : Spek({
    val scope by memoizedTestCoroutineScope()
    val state by memoized { UpdatableStateFlowImpl(0) }
    val incrementStateAction = IncrementStateAction()

    describe("A ActionManagerImpl") {
        val subject by memoized { ActionManagerImpl<Int>() }

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
