package se.gustavkarlsson.conveyor.actions

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
            runTest(UnconfinedTestDispatcher()) {
                val launchJob = launch {
                    subject.execute(flow)
                }
                flow.emit(1)
                launchJob.cancel()
            }
            expectThat(subject.collected).isEmpty()
        }

        it("collects current value when store subscribe count turns positive") {
            runTest(UnconfinedTestDispatcher()) {
                val launchJob = launch {
                    subject.execute(flow)
                }
                flow.storeSubscriberCount.value = 1
                launchJob.cancel()
            }
            expectThat(subject.collected).containsExactly(initialValue)
        }

        it("does not collect current value again when store subscribe count changes between positive values") {
            runTest(UnconfinedTestDispatcher()) {
                val launchJob = launch {
                    subject.execute(flow)
                }
                flow.storeSubscriberCount.value = 1
                flow.storeSubscriberCount.value = 2
                launchJob.cancel()
            }
            expectThat(subject.collected).containsExactly(initialValue)
        }

        it("collects current value again when store subscribe count changes between positive and 0") {
            runTest(UnconfinedTestDispatcher()) {
                val launchJob = launch {
                    subject.execute(flow)
                }
                flow.storeSubscriberCount.value = 1
                flow.storeSubscriberCount.value = 0
                flow.storeSubscriberCount.value = 1
                launchJob.cancel()
            }
            expectThat(subject.collected).containsExactly(initialValue, initialValue)
        }

        it("collects subsequent values when store subscribe count is positive") {
            runTest(UnconfinedTestDispatcher()) {
                val launchJob = launch {
                    subject.execute(flow)
                }
                flow.storeSubscriberCount.value = 1
                flow.emit(1)
                flow.emit(2)
                launchJob.cancel()
            }
            expectThat(subject.collected).containsExactly(initialValue, 1, 2)
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
