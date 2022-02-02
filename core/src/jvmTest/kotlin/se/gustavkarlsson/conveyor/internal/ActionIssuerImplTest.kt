package se.gustavkarlsson.conveyor.internal

import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import se.gustavkarlsson.conveyor.testing.NullAction
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.message

class ActionIssuerImplTest : FunSpec({
    val action = NullAction<Int>()
    val cancellationMessage = "Purposefully cancelled"
    val exception = CancellationException(cancellationMessage)
    val subject = ActionIssuerImpl<Int>()

    test("issuedActions suspends waiting for first item") {
        expectSuspends {
            subject.issuedActions.first()
        }
    }

    test("issuedActions emits action issued after subscribing") {
        runTest {
            val deferred = async { subject.issuedActions.first() }
            subject.issue(action)
            val result = deferred.await()
            expectThat(result).isEqualTo(action)
        }
    }

    test("issuedActions emits action issued before subscribing") {
        runTest {
            subject.issue(action)
            val result = subject.issuedActions.first()
            expectThat(result).isEqualTo(action)
        }
    }

    test("when cancelled, issuedActions emits error") {
        subject.cancel(exception)
        expectThrows<CancellationException> {
            subject.issuedActions.first()
        }.message.isEqualTo(cancellationMessage)
    }

    test("when cancelled, throws exception when action is issued") {
        subject.cancel(exception)
        expectThrows<CancellationException> {
            subject.issue(action)
        }.message.isEqualTo(cancellationMessage)
    }

    test("can be cancelled twice") {
        subject.cancel(Throwable())
        subject.cancel(Throwable())
    }
})

private fun expectSuspends(block: suspend () -> Unit) {
    expectThrows<TimeoutCancellationException> {
        withTimeout(100) {
            block()
        }
    }
}
