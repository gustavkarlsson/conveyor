package se.gustavkarlsson.conveyor

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class IssueTest : FunSpec({
    val store = Store(0)

    test("after started, issue extension function executes body as expected") {
        runTest {
            val runJob = launch { store.run() }
            runCurrent()
            store.issue { storeFlow ->
                storeFlow.update { it + 1 }
            }
            runCurrent()
            store.state.value.shouldBe(1)
            runJob.cancel()
        }
    }
})
