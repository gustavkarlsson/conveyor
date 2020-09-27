package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.withTimeout
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Change
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.test.FixedStateCommand
import se.gustavkarlsson.conveyor.test.NullAction
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.message

object CommandManagerTest : Spek({
    val initialState = "initial"
    val afterCommandState = "after_command"
    val bufferSize = 8
    val scope by memoized(
        factory = { TestCoroutineScope(Job()) },
        destructor = {
            it.cancel("Test ended")
            it.cleanupTestCoroutines()
        }
    )
    val command = FixedStateCommand(afterCommandState)
    val stateContainer by memoized { StateContainer(initialState) }

    describe("Creation") {
        it("throws exception with zero commandBufferSize") {
            expectThrows<IllegalArgumentException> {
                CommandManager(0, stateContainer)
            }.message
                .isNotNull()
                .contains("positive")
        }
        it("throws exception with negative commandBufferSize") {
            expectThrows<IllegalArgumentException> {
                CommandManager(-1, stateContainer)
            }.message
                .isNotNull()
                .contains("positive")
        }
    }
    describe("A Command Manager") {
        val subject by memoized { CommandManager(bufferSize, stateContainer) }

        it("state is initial") {
            expectThat(stateContainer.currentState).isEqualTo(initialState)
        }
        it("throws when issuing more commands than buffer contains") {
            expectThrows<IllegalStateException> {
                repeat(bufferSize + 1) {
                    subject.issue(command)
                }
            }
        }
        it("suspends on process") {
            expectSuspends {
                subject.process {}
            }
        }

        describe("that is processing") {
            val executedActions by memoized {
                mutableListOf<Action<String>>()
            }
            lateinit var processingJob: Job
            beforeEachTest {
                processingJob = scope.launch {
                    subject.process { executedActions += it }
                }
            }
            afterEachTest {
                processingJob.cancel("Test ended")
            }

            it("issued command reduces initial state") {
                val conditionalCommand = Command { state: String ->
                    if (state == initialState) {
                        Change(afterCommandState)
                    } else {
                        Change(initialState)
                    }
                }
                subject.issue(conditionalCommand)
                expectThat(stateContainer.currentState).isEqualTo(afterCommandState)
            }
            it("issued command with action executes action") {
                val action = NullAction<String>()
                val actionCommand = Command<String> {
                    Change(initialState, action)
                }
                subject.issue(actionCommand)
                expectThat(executedActions).containsExactly(action)
            }
            it("throws if processing again") {
                expectThrows<IllegalStateException> {
                    runBlockingTest {
                        subject.process {}
                    }
                }
            }
            it("actions are run in parallel") {

            }
        }

        describe("that was cancelled") {
            beforeEachTest {
                subject.cancel()
            }

            it("throws exception when command is issued") {
                expectThrows<IllegalStateException> {
                    subject.issue(command)
                }
            }
            it("does not suspend on process") {
                runBlockingTest {
                    subject.process {}
                }
            }
            it("can be cancelled again") {
                subject.cancel()
            }
        }
        describe("with a command issued") {
            beforeEachTest {
                subject.issue(command)
            }

            it("state is initial") {
                expectThat(stateContainer.currentState).isEqualTo(initialState)
            }

            describe("that was cancelled") {
                beforeEachTest { subject.cancel() }

                it("state does not change after processing") {
                    runBlockingTest {
                        subject.process {}
                    }
                    expectThat(stateContainer.currentState).isEqualTo(initialState)
                }
            }
            describe("that is processing") {
                lateinit var processingJob: Job
                beforeEachTest {
                    processingJob = scope.launch {
                        subject.process {}
                    }
                }
                afterEachTest {
                    processingJob.cancel("Test ended")
                }

                it("has new state") {
                    expectThat(stateContainer.currentState).isEqualTo(afterCommandState)
                }
            }
        }
    }
})

private fun expectSuspends(block: suspend () -> Unit) {
    runBlockingTest {
        expectThrows<TimeoutCancellationException> {
            withTimeout(100) {
                block()
            }
        }
    }
}

private class StateContainer<State>(override var currentState: State) : WriteableStateContainer<State>
