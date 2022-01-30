package se.gustavkarlsson.conveyor

import kotlinx.coroutines.test.runTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.testing.IncrementingAction
import se.gustavkarlsson.conveyor.testing.SimpleStoreFlow
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object ActionTest : Spek({
    val storeFlow by memoized { SimpleStoreFlow(0) }

    describe("An action created with lambda") {
        val subject by memoized { IncrementingAction(1) }

        it("runs when execute") {
            runTest {
                subject.execute(storeFlow)
            }
            expectThat(storeFlow.value).isEqualTo(1)
        }
    }
})
