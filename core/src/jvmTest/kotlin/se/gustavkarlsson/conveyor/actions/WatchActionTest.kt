package se.gustavkarlsson.conveyor.actions

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.TestAtomicStateFlow
import strikt.api.expectThat
import strikt.assertions.containsExactly

object WatchActionTest : Spek({
    val initialValue = 0
    val flow by memoized { TestAtomicStateFlow(initialValue) }

    describe("A test action") {
        val subject by memoized { TestWatchAction() }

        it("initially gets current value") {
            runBlockingTest {
                val launchJob = launch {
                    subject.execute(flow)
                }
                launchJob.cancel()
            }
            expectThat(subject.watched).containsExactly(initialValue)
        }

        it("watches new state changes") {
            runBlockingTest {
                val launchJob = launch {
                    subject.execute(flow)
                }
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
