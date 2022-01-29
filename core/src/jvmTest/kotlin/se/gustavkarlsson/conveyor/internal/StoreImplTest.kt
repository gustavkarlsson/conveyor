package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineExceptionHandler
import kotlinx.coroutines.test.createTestCoroutineScope
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException
import se.gustavkarlsson.conveyor.testing.IncrementingAction
import se.gustavkarlsson.conveyor.testing.SimpleStoreFlow
import se.gustavkarlsson.conveyor.testing.SuspendingProcess
import se.gustavkarlsson.conveyor.testing.TrackingActionIssuer
import se.gustavkarlsson.conveyor.testing.hasIssued
import se.gustavkarlsson.conveyor.testing.hasNeverBeenCancelled
import se.gustavkarlsson.conveyor.testing.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue
import kotlin.coroutines.EmptyCoroutineContext

object StoreImplTest : Spek({
    val initialState = 0
    val action = IncrementingAction(1)
    val state by memoized { SimpleStoreFlow(initialState) }
    val actionIssuer by memoized { TrackingActionIssuer<Int>() }

    describe("A minimal store") {
        val subject by memoized { StoreImpl(state, actionIssuer, listOf(SuspendingProcess)) }

        it("state.value returns current state") {
            val result = subject.state.value
            expectThat(result).isEqualTo(initialState)
        }
        it("state.first() returns current state") {
            val result = runBlockingTest {
                subject.state.value
            }
            expectThat(result).isEqualTo(initialState)
        }
        it("throws when action issued") {
            expectThrows<StoreNotYetStartedException> {
                subject.issue(action)
            }
        }

        describe("that was started") {
            val startScope by memoized { createTestCoroutineScope(TestCoroutineDispatcher() + TestCoroutineExceptionHandler() + EmptyCoroutineContext) }
            lateinit var job: Job
            beforeEachTest {
                job = startScope.launch { subject.run() }
            }
            afterEachTest {
                job.cancel("Test ended")
            }

            it("starting again throws exception") {
                expectThrows<StoreAlreadyStartedException> {
                    subject.run()
                }
            }
            it("issue issues action") {
                subject.issue(action)
                expectThat(actionIssuer).hasIssued(action)
            }
            it("nothing has been cancelled") {
                expectThat(actionIssuer).hasNeverBeenCancelled()
            }
            it("actions are issued") {
                subject.issue(action)
                expectThat(actionIssuer).hasIssued(action)
            }
            it("job is active") {
                expectThat(job).describedAs("job")
                    .get { isActive }.isTrue()
            }

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
        }
    }
})
