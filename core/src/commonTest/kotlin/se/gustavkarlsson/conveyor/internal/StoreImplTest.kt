package se.gustavkarlsson.conveyor.internal

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException
import se.gustavkarlsson.conveyor.testing.IncrementingAction
import se.gustavkarlsson.conveyor.testing.SimpleStoreFlow
import se.gustavkarlsson.conveyor.testing.SuspendingProcess
import se.gustavkarlsson.conveyor.testing.TrackingActionIssuer
import se.gustavkarlsson.conveyor.testing.shouldHaveIssued
import se.gustavkarlsson.conveyor.testing.shouldNeverHaveBeenCancelled

class StoreImplTest : FunSpec({
    val initialState = 0
    val action = IncrementingAction(1)
    val state = SimpleStoreFlow(initialState)
    val actionIssuer = TrackingActionIssuer<Int>()
    val cancellationException = CancellationException("Job cancelled at beginning of test")
    val subject = StoreImpl(state, actionIssuer, listOf(SuspendingProcess))

    test("state.value returns current state") {
        val result = subject.state.value
        result.shouldBe(initialState)
    }

    test("state.first() returns current state") {
        val result = subject.state.value
        result.shouldBe(initialState)
    }

    test("throws when action issued") {
        shouldThrow<StoreNotYetStartedException> {
            subject.issue(action)
        }
    }

    test("starting again throws exception") {
        runTest {
            val runJob = launch { subject.run() }
            runCurrent()
            shouldThrow<StoreAlreadyStartedException> {
                subject.run()
            }
            runJob.cancel()
        }
    }

    test("issue issues action") {
        runTest {
            val runJob = launch { subject.run() }
            runCurrent()
            subject.issue(action)
            runCurrent()
            actionIssuer.shouldHaveIssued(action)
            runJob.cancel()
        }
    }

    test("nothing has been cancelled") {
        runTest {
            val runJob = launch { subject.run() }
            actionIssuer.shouldNeverHaveBeenCancelled()
            runJob.cancel()
        }
    }

    test("actions are issued") {
        runTest {
            val runJob = launch { subject.run() }
            runCurrent()
            subject.issue(action)
            actionIssuer.shouldHaveIssued(action)
            runJob.cancel()
        }
    }

    test("stopping twice succeeds") {
        runTest {
            val runJob = launch { subject.run() }
            runJob.cancel()
            runJob.cancel()
        }
    }

    test("starting after stopped throws with cancellationException as reason") {
        runTest {
            val runJob = launch { subject.run() }
            runCurrent()
            runJob.cancel(cancellationException)
            runCurrent()
            val throwable = shouldThrow<StoreStoppedException> {
                subject.run()
            }
            assertSoftly {
                throwable.cancellationReason.shouldBeInstanceOf<CancellationException>()
                throwable.cancellationReason.message.shouldBe(cancellationException.message)
            }
        }
    }

    test("issuing action after stopped throws with cancellationException as reason") {
        runTest {
            val runJob = launch { subject.run() }
            runCurrent()
            runJob.cancel(cancellationException)
            runCurrent()
            val throwable = shouldThrow<StoreStoppedException> {
                subject.issue(action)
            }
            assertSoftly {
                throwable.cancellationReason.shouldBeInstanceOf<CancellationException>()
                throwable.cancellationReason.message.shouldBe(cancellationException.message)
            }
        }
    }

    test("actionIssuer has been cancelled by exception") {
        runTest {
            val runJob = launch { subject.run() }
            runCurrent()
            runJob.cancel(cancellationException)
            runCurrent()
            actionIssuer.cancellations.shouldHaveSize(1)
            assertSoftly {
                actionIssuer.cancellations.first().shouldBeInstanceOf<CancellationException>()
                actionIssuer.cancellations.first().message.shouldBe(cancellationException.message)
            }
        }
    }
})
