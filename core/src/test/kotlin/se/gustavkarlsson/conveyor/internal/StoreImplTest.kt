package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineScope
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreStoppedException
import se.gustavkarlsson.conveyor.StoreAlreadyStartedException
import se.gustavkarlsson.conveyor.StoreNotYetStartedException
import se.gustavkarlsson.conveyor.test.StateHoldingStateAccess
import se.gustavkarlsson.conveyor.test.TrackingActionIssuer
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

object StoreImplTest : Spek({
    val initialState = "initial"
    val secondState = "second"
    val action = Action<String> {}
    val stateFlowProvider by memoized { SimpleStateFlowProvider(initialState, secondState) }
    val stateAccess by memoized { StateHoldingStateAccess(initialState) }
    val actionIssuer by memoized { TrackingActionIssuer<String>() }
    val liveActionsCounter by memoized { TrackingLiveActionsCounter() }
    val foreverProcessor = object : ActionProcessor<String> {
        override suspend fun process(onAction: suspend (Action<String>) -> Unit) {
            delay(Long.MAX_VALUE)
        }
    }

    // TODO Add more tests with processors and cancellables
    describe("A minimal store") {
        val subject by memoized {
            StoreImpl(
                stateFlowProvider = stateFlowProvider,
                stateAccess = stateAccess,
                actionIssuer = actionIssuer,
                liveActionsCounter = liveActionsCounter,
                actionProcessors = listOf(foreverProcessor),
                actionMappers = emptyList(),
                cancellables = emptyList(),
            )
        }

        it("currentState returns current state") {
            val result = subject.currentState
            expectThat(result).isEqualTo(initialState)
        }
        it("state returns state") {
            val result = runBlockingTest {
                subject.state.toList()
            }
            expectThat(result).containsExactly(initialState, secondState)
        }
        it("throws when action issued") {
            expectThrows<StoreNotYetStartedException> {
                subject.issue(action)
            }
        }

        describe("that was opened") {
            val openScope by memoized { TestCoroutineScope() }
            lateinit var job: Job
            beforeEachTest {
                job = subject.start(openScope)
            }
            afterEachTest {
                job.cancel("Test ended")
            }

            it("opening again throws exception") {
                expectThrows<StoreAlreadyStartedException> {
                    subject.start(openScope)
                }
            }
            it("issueAction issues action") {
                runBlockingTest {
                    subject.issue(action)
                }
                expectThat(actionIssuer.issuedActions).containsExactly(action)
            }

            describe("that was stopped") {
                val cancellationException by memoized { CancellationException("Job cancelled at beginning of test") }
                beforeEachTest { job.cancel(cancellationException) }

                it("stopping again succeeds") {
                    job.cancel("Stopped again")
                }
                it("open again throws with cancellationException as reason") {
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

private class SimpleStateFlowProvider<T>(firstState: T, secondState: T) : StateFlowProvider<T> {
    override val stateFlow: Flow<T> = flowOf(firstState, secondState)
}

private class TrackingLiveActionsCounter(var count: Int = 0) : LiveActionsCounter {
    override fun increment() {
        count++
    }

    override fun decrement() {
        count--
    }
}
