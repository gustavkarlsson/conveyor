package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScope
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException
import se.gustavkarlsson.conveyor.testing.IncrementingAction
import se.gustavkarlsson.conveyor.testing.TrackingActionManager
import se.gustavkarlsson.conveyor.testing.hasBeenCancelledWith
import se.gustavkarlsson.conveyor.testing.hasIssued
import se.gustavkarlsson.conveyor.testing.hasNeverBeenCancelled
import se.gustavkarlsson.conveyor.testing.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

object StoreImplTest : Spek({
    val initialState = 0
    val action = IncrementingAction(1)
    val state by memoized { UpdatableStateFlowImpl(initialState) }
    val actionManager by memoized { TrackingActionManager<Int>() }

    describe("A minimal store") {
        val subject by memoized { StoreImpl(state, actionManager, emptyList()) } // FIXME Test startActions

        it("state.value returns current state") {
            val result = subject.state.value
            expectThat(result).isEqualTo(initialState)
        }
        it("state returns current state") {
            val result = runBlockingTest {
                subject.state.first()
            }
            expectThat(result).isEqualTo(initialState)
        }
        it("throws when action issued") {
            expectThrows<StoreNotYetStartedException> {
                subject.issue(action)
            }
        }

        describe("that was started") {
            val startScope by memoized { TestCoroutineScope() }
            lateinit var job: Job
            beforeEachTest {
                job = subject.start(startScope)
            }
            afterEachTest {
                job.cancel("Test ended")
            }

            it("starting again throws exception") {
                expectThrows<StoreAlreadyStartedException> {
                    subject.start(startScope)
                }
            }
            it("issue issues action") {
                subject.issue(action)
                expectThat(actionManager).hasIssued(action)
            }
            it("nothing has been cancelled") {
                expectThat(actionManager).hasNeverBeenCancelled()
            }
            it("actions are processed when issued") {
                subject.issue(action)
                val result = subject.state.value
                expectThat(result).isEqualTo(1)
            }

            describe("that was stopped") {
                val cancellationException = CancellationException("Job cancelled at beginning of test")
                beforeEachTest { job.cancel(cancellationException) }

                it("stopping again succeeds") {
                    job.cancel("Stopped again")
                }
                it("start again throws with cancellationException as reason") {
                    expectThrows<StoreStoppedException> {
                        runBlockingTest {
                            subject.start(this)
                        }
                    }.get { cancellationReason }.isEqualTo(cancellationException)
                }
                it("issuing action throws with cancellationException as reason") {
                    expectThrows<StoreStoppedException> {
                        subject.issue(action)
                    }.get { cancellationReason }.isEqualTo(cancellationException)
                }
                it("actionManager has been cancelled by exception") {
                    expectThat(actionManager).hasBeenCancelledWith(cancellationException)
                }
            }
        }
    }
})
