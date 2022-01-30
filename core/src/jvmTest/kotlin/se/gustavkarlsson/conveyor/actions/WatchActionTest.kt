package se.gustavkarlsson.conveyor.actions

import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import se.gustavkarlsson.conveyor.test.TestStoreFlow
import strikt.api.expectThat
import strikt.assertions.containsExactly

object WatchActionTest : FunSpec({
    val initialValue = 0
    val flow = TestStoreFlow(initialValue)
    val subject = TestWatchAction()

    test("initially gets current value") {
        runTest {
            val job = launch {
                subject.execute(flow)
            }
            runCurrent()
            expectThat(subject.watched).containsExactly(initialValue)
            job.cancel()
        }
    }

    test("watches new state changes") {
        runTest {
            val job = launch {
                subject.execute(flow)
            }
            runCurrent()
            flow.emit(1)
            flow.emit(2)
            runCurrent()
            expectThat(subject.watched).containsExactly(initialValue, 1, 2)
            job.cancel()
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
