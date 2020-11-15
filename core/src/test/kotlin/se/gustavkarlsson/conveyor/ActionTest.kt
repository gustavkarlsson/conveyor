package se.gustavkarlsson.conveyor

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.internal.UpdatableStateFlowImpl
import se.gustavkarlsson.conveyor.testing.runBlockingTest
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object ActionTest : Spek({
    val state by memoized { UpdatableStateFlowImpl(0) }

    describe("An action created with lambda") {
        val subject by memoized {
            Action<Int> { state ->
                state.update { this + 1 }
            }
        }

        it("runs when execute") {
            runBlockingTest {
                subject.execute(state)
            }
            expectThat(state.value).isEqualTo(1)
        }
    }
})
