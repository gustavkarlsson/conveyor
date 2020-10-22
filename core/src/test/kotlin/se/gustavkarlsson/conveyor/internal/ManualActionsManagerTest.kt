package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.*
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.test.memoizedTestCoroutineScope
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly

object ManualActionsManagerTest : Spek({
    val scope by memoizedTestCoroutineScope()
    val action = Action<String> {}
    describe("A ManualActionsManager") {
        val subject by memoized { ManualActionsManager<String>() }

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

            it("issued action executes") {
                subject.issue(action)
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

            it("throws exception when action is issued") {
                expectThrows<IllegalStateException> {
                    subject.issue(action)
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
