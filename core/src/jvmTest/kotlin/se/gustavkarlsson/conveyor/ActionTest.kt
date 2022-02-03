package se.gustavkarlsson.conveyor

import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.test.runTest
import se.gustavkarlsson.conveyor.testing.IncrementingAction
import se.gustavkarlsson.conveyor.testing.SimpleStoreFlow
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ActionTest : FunSpec({
    val storeFlow = SimpleStoreFlow(0)
    val subject = IncrementingAction(1)

    test("runs when execute") {
        runTest {
            subject.execute(storeFlow)
            expectThat(storeFlow.value).isEqualTo(1)
        }
    }
})
