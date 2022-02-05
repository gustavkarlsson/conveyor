package se.gustavkarlsson.conveyor.internal

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import se.gustavkarlsson.conveyor.testing.NullAction

class ActionIssuerImplTest : FunSpec({
    val action = NullAction<Int>()
    val cancellationMessage = "Purposefully cancelled"
    val exception = CancellationException(cancellationMessage)
    val subject = ActionIssuerImpl<Int>()

    test("issuedActions suspends waiting for first item") {
        runTest {
            val job = launch { subject.issuedActions.first() }
            runCurrent()
            job.isActive.shouldBeTrue()
            job.cancel()
        }
    }

    test("issuedActions emits action issued after subscribing") {
        runTest {
            val deferred = async { subject.issuedActions.first() }
            subject.issue(action)
            val result = deferred.await()
            result.shouldBe(action)
        }
    }

    test("issuedActions emits action issued before subscribing") {
        runTest {
            subject.issue(action)
            val result = subject.issuedActions.first()
            result.shouldBe(action)
        }
    }

    test("when cancelled, issuedActions emits error") {
        subject.cancel(exception)
        shouldThrow<CancellationException> {
            subject.issuedActions.first()
        }.message.shouldBe(cancellationMessage)
    }

    test("when cancelled, throws exception when action is issued") {
        subject.cancel(exception)
        shouldThrow<CancellationException> {
            subject.issue(action)
        }.message.shouldBe(cancellationMessage)
    }

    test("can be cancelled twice") {
        subject.cancel(Throwable())
        subject.cancel(Throwable())
    }
})
