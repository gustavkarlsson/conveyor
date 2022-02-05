package se.gustavkarlsson.conveyor.internal

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo

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
        expectThrows<StoreNotYetStartedException> {
            subject.issue(action)
        }
    }

    test("starting again throws exception") {
        runTest {
            val runJob = launch { subject.run() }
            runCurrent()
            expectThrows<StoreAlreadyStartedException> {
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
            expectThrows<StoreStoppedException> {
                subject.run()
            }.get { cancellationReason }.describedAs("cancellation reason")
                .and {
                    isA<CancellationException>()
                    get { message }.describedAs("message")
                        .isEqualTo(cancellationException.message)
                }
        }
    }

    test("issuing action after stopped throws with cancellationException as reason") {
        runTest {
            val runJob = launch { subject.run() }
            runCurrent()
            runJob.cancel(cancellationException)
            runCurrent()
            expectThrows<StoreStoppedException> {
                subject.issue(action)
            }.get { cancellationReason }.describedAs("cancellation reason")
                .and {
                    isA<CancellationException>()
                    get { message }.describedAs("message")
                        .isEqualTo(cancellationException.message)
                }
        }
    }

    test("actionIssuer has been cancelled by exception") {
        runTest {
            val runJob = launch { subject.run() }
            runCurrent()
            runJob.cancel(cancellationException)
            runCurrent()
            expectThat(actionIssuer.cancellations).describedAs("cancellations")
                .hasSize(1)
                .first().and {
                    isA<CancellationException>()
                    get { message }.describedAs("message")
                        .isEqualTo(cancellationException.message)
                }
        }
    }
})
