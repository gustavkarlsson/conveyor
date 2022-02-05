package se.gustavkarlsson.conveyor

import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.test.runTest
import se.gustavkarlsson.conveyor.testing.IncrementingAction
import se.gustavkarlsson.conveyor.testing.SimpleStoreFlow
import io.kotest.matchers.shouldBe

class ActionTest : FunSpec({
    val storeFlow = SimpleStoreFlow(0)
    val subject = IncrementingAction(1)

    test("runs when execute") {
        runTest {
            subject.execute(storeFlow)
            storeFlow.value.shouldBe(1)
        }
    }
})
