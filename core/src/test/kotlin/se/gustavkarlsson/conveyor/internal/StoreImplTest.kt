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
import se.gustavkarlsson.conveyor.action
import se.gustavkarlsson.conveyor.test.DelayingTrackingActionProcessor
import se.gustavkarlsson.conveyor.test.SimpleStateAccess
import se.gustavkarlsson.conveyor.test.TrackingActionIssuer
import se.gustavkarlsson.conveyor.test.TrackingCancellable
import se.gustavkarlsson.conveyor.test.hasBeenCancelledWith
import se.gustavkarlsson.conveyor.test.hasCompletedCount
import se.gustavkarlsson.conveyor.test.hasIssued
import se.gustavkarlsson.conveyor.test.hasNeverBeenCancelled
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

object StoreImplTest : Spek({
    val initialState = 0
    val action = action<Int> {}
    val stateAccess by memoized { SimpleStateAccess(initialState) }
    val actionIssuer by memoized { TrackingActionIssuer<Int>() }
    val liveActionsCounter by memoized { TrackingLiveActionsCounter() }
    val processor by memoized { DelayingTrackingActionProcessor<Int>() }
    val cancellable by memoized { TrackingCancellable() }

    describe("A minimal store") {
        val subject by memoized {
            StoreImpl(
                stateAccess = stateAccess,
                actionIssuer = actionIssuer,
                liveActionsCounter = liveActionsCounter,
                actionProcessors = listOf(processor, processor),
                cancellables = listOf(cancellable),
            )
        }

        it("currentState returns current state") {
            val result = subject.currentState
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
                runBlockingTest {
                    subject.issue(action)
                }
                expectThat(actionIssuer).hasIssued(action)
            }
            it("processors run in parallel") {
                expectThat(processor).hasCompletedCount(0)
                startScope.advanceTimeBy(1)
                expectThat(processor).hasCompletedCount(2)
            }
            it("nothing has been cancelled") {
                expectThat(cancellable).hasNeverBeenCancelled()
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
                it("cancellable has been cancelled by exception") {
                    expectThat(cancellable).hasBeenCancelledWith(cancellationException)
                }
            }
        }
    }
})

private class TrackingLiveActionsCounter(var count: Int = 0) : LiveActionsCounter {
    override fun increment() {
        count++
    }

    override fun decrement() {
        count--
    }
}
