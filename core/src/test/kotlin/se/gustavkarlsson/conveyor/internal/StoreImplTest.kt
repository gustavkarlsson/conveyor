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
import se.gustavkarlsson.conveyor.StoreClosedException
import se.gustavkarlsson.conveyor.StoreOpenedException
import se.gustavkarlsson.conveyor.test.FixedStateCommand
import se.gustavkarlsson.conveyor.test.TrackingCommandIssuer
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

object StoreImplTest : Spek({
    val initialState = "initial"
    val secondState = "second"
    val command = FixedStateCommand("dummy")
    val stateContainer by memoized { SimpleStateManager(initialState, secondState) }
    val commandIssuer by memoized { TrackingCommandIssuer<String>() }
    val liveActionsCounter by memoized { TrackingLiveActionsCounter() }
    val foreverProcessor = object : Processor<String> {
        override suspend fun process(onAction: suspend (Action<String>) -> Unit) {
            delay(Long.MAX_VALUE)
        }
    }

    // TODO Add more tests with processors and cancellables
    describe("A minimal store") {
        val subject by memoized {
            StoreImpl(
                stateContainer,
                commandIssuer,
                liveActionsCounter,
                processors = listOf(foreverProcessor),
                cancellables = emptyList()
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
        it("issueCommand issues command") {
            runBlockingTest {
                subject.issue(command)
            }
            expectThat(commandIssuer.issuedCommands).containsExactly(command)
        }

        describe("that was opened") {
            val openScope by memoized { TestCoroutineScope() }
            lateinit var job: Job
            beforeEachTest {
                job = subject.open(openScope)
            }
            afterEachTest {
                job.cancel("Test ended")
            }

            it("opening again throws exception") {
                expectThrows<StoreOpenedException> {
                    subject.open(openScope)
                }
            }
            it("issueCommand issues command") {
                runBlockingTest {
                    subject.issue(command)
                }
                expectThat(commandIssuer.issuedCommands).containsExactly(command)
            }

            describe("that was closed") {
                val cancellationException by memoized { CancellationException() }
                beforeEachTest { job.cancel(cancellationException) }

                it("closing again succeeds") {
                    job.cancel()
                }
                it("open again throws with cancellationException as reason") {
                    expectThrows<StoreClosedException> {
                        runBlockingTest {
                            subject.open(this)
                        }
                    }.get { reason }.isEqualTo(cancellationException)
                }
                it("issuing command throws with cancellationException as reason") {
                    expectThrows<StoreClosedException> {
                        subject.issue(command)
                    }.get { reason }.isEqualTo(cancellationException)
                }
            }
        }
    }
})

private class SimpleStateManager<T>(override val currentState: T, secondState: T) : ReadableStateContainer<T> {
    override val state: Flow<T> = flowOf(currentState, secondState)
}

private class TrackingLiveActionsCounter(var count: Int = 0) : LiveActionsCounter {
    override fun increment() {
        count++
    }

    override fun decrement() {
        count--
    }
}
