package se.gustavkarlsson.conveyor

import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object IssueTest : Spek({
    describe("A store") {
        val store by memoized {
            Store(0)
        }

        it("after started, issue extension function executes body as expected") {
            runTest {
                launch { store.run() }
                runCurrent()
                store.issue { storeFlow ->
                    storeFlow.update { it + 1 }
                }
                runCurrent()
                expectThat(store.state.value).isEqualTo(1)
                cancel()
            }
        }
    }
})
