package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.withTimeout
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Change
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.actions.SingleAction
import se.gustavkarlsson.conveyor.test.FixedStateCommand
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.message
import java.util.concurrent.atomic.AtomicReference

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
    val processingScope by memoized(
        factory = { TestCoroutineScope(Job()) },
        destructor = {
            it.cancel("Test ended")
            it.cleanupTestCoroutines()
        }
    )
    val command = FixedStateCommand(afterCommandState)
    val stateHolder by memoized { AtomicReference(initialState) }

    describe("Creation") {
        it("throws exception with zero commandBufferSize") {
            expectThrows<IllegalArgumentException> {
                CommandManager(0, stateHolder::get, stateHolder::set)
            }.message
                .isNotNull()
                .contains("positive")
        }
        it("throws exception with negative commandBufferSize") {
            expectThrows<IllegalArgumentException> {
                CommandManager(-1, stateHolder::get, stateHolder::set)
            }.message
                .isNotNull()
                .contains("positive")
        }
    }
    describe("A Command Manager") {
        val subject by memoized { CommandManager(bufferSize, stateHolder::get, stateHolder::set) }

        it("state is initial") {
            expectThat(stateHolder.get()).isEqualTo(initialState)
        }
        it("does not suspend when issuing commands to fill the buffer") {
            runBlockingTest {
                repeat(bufferSize) {
                    subject.issue(command)
                }
            }
        }
        it("suspends when issuing more commands than buffer contains") {
            expectSuspends {
                repeat(bufferSize + 1) {
                    subject.issue(command)
                }
            }
        }
        it("suspends on process") {
            expectSuspends {
                subject.process(scope)
            }
        }

        describe("that is processing") {
            lateinit var processingJob: Job
            beforeEachTest {
                processingJob = processingScope.launch {
                    subject.process(this)
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
                runBlockingTest {
                    subject.issue(conditionalCommand)
                }
                expectThat(stateHolder.get()).isEqualTo(afterCommandState)
            }
            it("issued command with action changes state") {
                val actionCommand = Command<String> {
                    Change(initialState, SingleAction { command })
                }
                runBlockingTest {
                    subject.issue(actionCommand)
                }
                expectThat(stateHolder.get()).isEqualTo(afterCommandState)
            }
            it("throws if processing again") {
                expectThrows<IllegalStateException> {
                    runBlockingTest {
                        subject.process(processingScope)
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
                    runBlockingTest {
                        subject.issue(command)
                    }
                }
            }
            it("does not suspend on process") {
                runBlockingTest {
                    subject.process(scope)
                }
            }
            it("can be cancelled again") {
                subject.cancel()
            }
        }
        describe("with a command issued") {
            beforeEachTest {
                runBlockingTest {
                    subject.issue(command)
                }
            }

            it("state is initial") {
                expectThat(stateHolder.get()).isEqualTo(initialState)
            }

            describe("that was cancelled") {
                beforeEachTest { subject.cancel() }

                it("state does not change after processing") {
                    runBlockingTest {
                        subject.process(scope)
                    }
                    expectThat(stateHolder.get()).isEqualTo(initialState)
                }
            }
            describe("that is processing") {
                lateinit var processingJob: Job
                beforeEachTest {
                    processingJob = processingScope.launch {
                        subject.process(this)
                    }
                }
                afterEachTest {
                    processingJob.cancel("Test ended")
                }

                it("has new state") {
                    expectThat(stateHolder.get()).isEqualTo(afterCommandState)
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
