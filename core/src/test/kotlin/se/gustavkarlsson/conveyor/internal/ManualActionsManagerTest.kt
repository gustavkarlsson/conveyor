package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.action
import se.gustavkarlsson.conveyor.test.memoizedTestCoroutineScope
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly

object ManualActionsManagerTest : Spek({
    val scope by memoizedTestCoroutineScope()
    val action = action<String> {}
    describe("A ManualActionsManager") {
        val subject by memoized { ManualActionsManager<String>() }

        it("suspends on collecting actionFlow") {
            expectSuspends {
                subject.actionFlow.collect {}
            }
        }

        describe("that is collecting actionFlow") {
            val executedActions by memoized {
                mutableListOf<Action<String>>()
            }
            lateinit var collectingJob: Job
            beforeEachTest {
                collectingJob = scope.launch {
                    subject.actionFlow.collect { executedActions += it }
                }
            }
            afterEachTest {
                collectingJob.cancel("Test ended")
            }

            it("issued action executes") {
                subject.issue(action)
                expectThat(executedActions).containsExactly(action)
            }
            it("throws if collecting actionFlow again") {
                expectThrows<IllegalStateException> {
                    runBlockingTest {
                        subject.actionFlow.collect {}
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
            it("does not suspend on collecting actionFlow") {
                runBlockingTest {
                    subject.actionFlow.collect {}
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
