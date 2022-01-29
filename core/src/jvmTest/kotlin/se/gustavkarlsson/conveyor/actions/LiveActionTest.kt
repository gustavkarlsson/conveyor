package se.gustavkarlsson.conveyor.actions

import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.StoreFlow
import se.gustavkarlsson.conveyor.test.TestStoreFlow
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

object LiveActionTest : Spek({
    val initialValue = 0
    val flow by memoized { TestStoreFlow(initialValue) }

    describe("A test action") {
        val subject by memoized { TestLiveAction() }

        it("does not collect when store subscriber count is 0") {
            runTest {
                launch {
                    subject.execute(flow)
                }
                runCurrent()
                flow.emit(1)
                runCurrent()
                expectThat(subject.collected).isEmpty()
                cancel()
            }
        }

        it("collects current value when store subscribe count turns positive") {
            runTest {
                launch {
                    subject.execute(flow)
                }
                runCurrent()
                flow.storeSubscriberCount.value = 1
                runCurrent()
                expectThat(subject.collected).containsExactly(initialValue)
                cancel()
            }
        }

        it("does not collect current value again when store subscribe count changes between positive values") {
            runTest {
                launch {
                    subject.execute(flow)
                }
                runCurrent()
                flow.storeSubscriberCount.value = 1
                runCurrent()
                flow.storeSubscriberCount.value = 2
                runCurrent()
                expectThat(subject.collected).containsExactly(initialValue)
                cancel()
            }
        }

        it("collects current value again when store subscribe count changes between positive and 0") {
            runTest {
                launch {
                    subject.execute(flow)
                }
                runCurrent()
                flow.storeSubscriberCount.value = 1
                runCurrent()
                flow.storeSubscriberCount.value = 0
                runCurrent()
                flow.storeSubscriberCount.value = 1
                runCurrent()
                expectThat(subject.collected).containsExactly(initialValue, initialValue)
                cancel()
            }
        }

        it("collects subsequent values when store subscribe count is positive") {
            runTest(UnconfinedTestDispatcher()) {
                launch {
                    subject.execute(flow)
                }
                runCurrent()
                flow.storeSubscriberCount.value = 1
                runCurrent()
                flow.emit(1)
                runCurrent()
                flow.emit(2)
                runCurrent()
                expectThat(subject.collected).containsExactly(initialValue, 1, 2)
                cancel()
            }
        }
    }
})

private class TestLiveAction : LiveAction<Int>() {
    private val _collected = mutableListOf<Int>()
    val collected: List<Int> = _collected

    override suspend fun onLive(storeFlow: StoreFlow<Int>) {
        storeFlow.collect { state ->
            _collected += state
        }
    }
}
