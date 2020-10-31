package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineScope
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.StoreStoppedException
import se.gustavkarlsson.conveyor.action
import se.gustavkarlsson.conveyor.test.SimpleStateAccess
import se.gustavkarlsson.conveyor.test.TrackingActionIssuer
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

object StoreImplTest : Spek({
    val initialState = "initial"
    val action = action<String> {}
    val stateAccess by memoized { SimpleStateAccess(initialState) }
    val actionIssuer by memoized { TrackingActionIssuer<String>() }
    val liveActionsCounter by memoized { TrackingLiveActionsCounter() }

    // TODO Add more tests with processors and cancellables
    describe("A minimal store") {
        val subject by memoized {
            StoreImpl(
                stateAccess = stateAccess,
                actionIssuer = actionIssuer,
                liveActionsCounter = liveActionsCounter,
                actionFlow = flow { delay(Long.MAX_VALUE) }, // FIXME empty?
                cancellables = emptyList(),
            )
        }

        it("currentState returns current state") {
            val result = subject.currentState
            expectThat(result).isEqualTo(initialState)
        }
        it("state returns state") {
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
            it("issueAction issues action") {
                runBlockingTest {
                    subject.issue(action)
                }
                expectThat(actionIssuer.issuedActions).containsExactly(action)
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
