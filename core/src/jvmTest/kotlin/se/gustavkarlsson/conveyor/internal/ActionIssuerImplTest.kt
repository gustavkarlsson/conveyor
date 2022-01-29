package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.testing.NullAction
import se.gustavkarlsson.conveyor.testing.runTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.message

object ActionIssuerImplTest : Spek({
    val action = NullAction<Int>()

    describe("A ActionIssuerImpl") {
        val subject by memoized { ActionIssuerImpl<Int>() }

        it("issuedActions suspends waiting for first item") {
            expectSuspends {
                subject.issuedActions.first()
            }
        }
        it("issuedActions emits action issued after subscribing") {
            val result = runTest {
                val deferred = async { subject.issuedActions.first() }
                subject.issue(action)
                deferred.await()
            }
            expectThat(result).isEqualTo(action)
        }
        it("issuedActions emits action issued before subscribing") {
            val result = runTest {
                subject.issue(action)
                subject.issuedActions.first()
            }
            expectThat(result).isEqualTo(action)
        }

        describe("that was cancelled") {
            val cancellationMessage = "Purposefully cancelled"
            val exception = CancellationException(cancellationMessage)
            beforeEachTest {
                subject.cancel(exception)
            }

            it("issuedActions emits error") {
                expectThrows<CancellationException> {
                    subject.issuedActions.first()
                }.message.isEqualTo(cancellationMessage)
            }
            it("throws exception when action is issued") {
                expectThrows<CancellationException> {
                    subject.issue(action)
                }.message.isEqualTo(cancellationMessage)
            }
            it("can be cancelled again") {
                subject.cancel(Throwable())
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
