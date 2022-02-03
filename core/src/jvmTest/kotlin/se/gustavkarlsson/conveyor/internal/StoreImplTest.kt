package se.gustavkarlsson.conveyor.internal

import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.testing.IncrementingAction
import se.gustavkarlsson.conveyor.testing.SimpleStoreFlow
import se.gustavkarlsson.conveyor.testing.SuspendingProcess
import se.gustavkarlsson.conveyor.testing.TrackingActionIssuer
import se.gustavkarlsson.conveyor.testing.hasIssued
import se.gustavkarlsson.conveyor.testing.hasNeverBeenCancelled
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class StoreImplTest : FunSpec({
    val initialState = 0
    val action = IncrementingAction(1)
    val state = SimpleStoreFlow(initialState)
    val actionIssuer = TrackingActionIssuer<Int>()
    val subject = StoreImpl(state, actionIssuer, listOf(SuspendingProcess))

    test("state.value returns current state") {
        val result = subject.state.value
        expectThat(result).isEqualTo(initialState)
    }

    test("state.first() returns current state") {
        val result = subject.state.value
        expectThat(result).isEqualTo(initialState)
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
            expectThat(actionIssuer).hasIssued(action)
            runJob.cancel()
        }
    }

    test("nothing has been cancelled") {
        runTest {
            val runJob = launch { subject.run() }
            expectThat(actionIssuer).hasNeverBeenCancelled()
            runJob.cancel()
        }
    }

    test("actions are issued") {
        runTest {
            val runJob = launch { subject.run() }
            runCurrent()
            subject.issue(action)
            expectThat(actionIssuer).hasIssued(action)
            runJob.cancel()
        }
    }
/*
FIXME can't get these to work

            describe("that was stopped") {
                val cancellationException = CancellationException("Job cancelled at beginning of test")
                beforeEachTest { job.cancel(cancellationException) }

                it("stopping again succeeds") {
                    job.cancel("Stopped again")
                }
                it("start again throws with cancellationException as reason") {
                    expectThrows<StoreStoppedException> {
                        subject.run()
                    }.get { cancellationReason }.describedAs("cancellation reason")
                        .and {
                            isA<CancellationException>()
                            get { message }.describedAs("message")
                                .isEqualTo(cancellationException.message)
                        }
                }
                it("issuing action throws with cancellationException as reason") {
                    expectThrows<StoreStoppedException> {
                        subject.issue(action)
                    }.get { cancellationReason }.describedAs("cancellation reason")
                        .and {
                            isA<CancellationException>()
                            get { message }.describedAs("message")
                                .isEqualTo(cancellationException.message)
                        }
                }
                it("actionIssuer has been cancelled by exception") {
                    expectThat(actionIssuer.cancellations).describedAs("cancellations")
                        .hasSize(1)
                        .first().and {
                            isA<CancellationException>()
                            get { message }.describedAs("message")
                                .isEqualTo(cancellationException.message)
                        }
                }
                it("job is cancelled") {
                    expectThat(job).describedAs("job")
                        .get { isCancelled }.isTrue()
                }
            }
*/
})
