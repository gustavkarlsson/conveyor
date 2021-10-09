package se.gustavkarlsson.conveyor

import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.testing.memoizedTestCoroutineScope
import se.gustavkarlsson.conveyor.testing.runBlockingTest
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object IssueTest : Spek({
    val scope by memoizedTestCoroutineScope()

    describe(  "A store that was started") {
        val store by memoized {
            Store(0).also { store ->
                scope.launch { store.run() }
            }
        }

        it("issue extension function executes body as expected") {
            runBlockingTest {
                store.issue { storeFlow ->
                    storeFlow.update { it + 1 }
                }
            }
            expectThat(store.state.value).isEqualTo(1)
        }
    }
})
