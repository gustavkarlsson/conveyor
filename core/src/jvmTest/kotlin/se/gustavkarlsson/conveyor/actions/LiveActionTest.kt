package se.gustavkarlsson.conveyor.actions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import se.gustavkarlsson.conveyor.StoreFlow
import se.gustavkarlsson.conveyor.test.TestStoreFlow

class LiveActionTest : FunSpec({
    val initialValue = 0
    val flow = TestStoreFlow(initialValue)
    val subject = TestLiveAction()

    test("does not collect when store subscriber count is 0") {
        runTest {
            val executeJob = launch {
                subject.execute(flow)
            }
            runCurrent()
            flow.emit(1)
            runCurrent()
            subject.collected.shouldBeEmpty()
            executeJob.cancel()
        }
    }

    test("collects current value when store subscribe count turns positive") {
        runTest {
            val executeJob = launch {
                subject.execute(flow)
            }
            runCurrent()
            flow.storeSubscriberCount.value = 1
            runCurrent()
            subject.collected.shouldContainExactly(initialValue)
            executeJob.cancel()
        }
    }

    test("does not collect current value again when store subscribe count changes between positive values") {
        runTest {
            val executeJob = launch {
                subject.execute(flow)
            }
            runCurrent()
            flow.storeSubscriberCount.value = 1
            runCurrent()
            flow.storeSubscriberCount.value = 2
            runCurrent()
            subject.collected.shouldContainExactly(initialValue)
            executeJob.cancel()
        }
    }

    test("collects current value again when store subscribe count changes between positive and 0") {
        runTest {
            val executeJob = launch {
                subject.execute(flow)
            }
            runCurrent()
            flow.storeSubscriberCount.value = 1
            runCurrent()
            flow.storeSubscriberCount.value = 0
            runCurrent()
            flow.storeSubscriberCount.value = 1
            runCurrent()
            subject.collected.shouldContainExactly(initialValue, initialValue)
            executeJob.cancel()
        }
    }

    test("collects subsequent values when store subscribe count is positive") {
        runTest {
            val executeJob = launch {
                subject.execute(flow)
            }
            runCurrent()
            flow.storeSubscriberCount.value = 1
            runCurrent()
            flow.emit(1)
            runCurrent()
            flow.emit(2)
            runCurrent()
            subject.collected.shouldContainExactly(initialValue, 1, 2)
            executeJob.cancel()
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
