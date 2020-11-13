package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.action
import se.gustavkarlsson.conveyor.testing.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.message

object ActionManagerImplTest : Spek({
    val action = action<Int> {}

    describe("A ActionManagerImpl") {
        val subject by memoized { ActionManagerImpl<Int>() }

        it("actions suspends waiting for first item") {
            expectSuspends {
                subject.actions.first()
            }
        }
        it("actions emits action issued after subscribing") {
            val result = runBlockingTest {
                val deferred = async { subject.actions.first() }
                subject.issue(action)
                deferred.await()
            }
            expectThat(result).isEqualTo(action)
        }
        it("actions emits action issued before subscribing") {
            val result = runBlockingTest {
                subject.issue(action)
                subject.actions.first()
            }
            expectThat(result).isEqualTo(action)
        }

        describe("that was cancelled") {
            val cancellationMessage = "Purposefully cancelled"
            val exception = CancellationException(cancellationMessage)
            beforeEachTest {
                subject.cancel(exception)
            }

            it("actions emits error") {
                expectThrows<CancellationException> {
                    subject.actions.first()
                }.message.isEqualTo(cancellationMessage)
            }
            it("throws exception when action is issued") {
                expectThrows<CancellationException> {
                    subject.issue(action)
                }.message.isEqualTo(cancellationMessage)
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
