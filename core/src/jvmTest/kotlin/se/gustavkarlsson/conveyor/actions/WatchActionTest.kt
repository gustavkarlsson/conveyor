package se.gustavkarlsson.conveyor.actions

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.TestStoreFlow
import strikt.api.expectThat
import strikt.assertions.containsExactly

object WatchActionTest : Spek({
    val initialValue = 0
    val flow by memoized { TestStoreFlow(initialValue) }

    describe("A test action") {
        val subject by memoized { TestWatchAction() }

        it("initially gets current value") {
            runTest() {
                val launchJob = launch {
                    subject.execute(flow)
                }
                runCurrent()
                launchJob.cancel()
            }
            expectThat(subject.watched).containsExactly(initialValue)
        }

        it("watches new state changes") {
            runTest {
                val launchJob = launch {
                    subject.execute(flow)
                }
                runCurrent()
                flow.emit(1)
                flow.emit(2)
                launchJob.cancel()
            }
            expectThat(subject.watched).containsExactly(initialValue, 1, 2)
        }
    }
})

private class TestWatchAction : WatchAction<Int>() {
    private val _watched = mutableListOf<Int>()
    val watched: List<Int> = _watched

    override fun onState(state: Int) {
        _watched += state
    }
}
