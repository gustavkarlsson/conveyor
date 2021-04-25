package se.gustavkarlsson.conveyor.actions

import org.junit.jupiter.api.fail
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.AtomicStateFlow
import se.gustavkarlsson.conveyor.test.TestAtomicStateFlow

object LiveActionTest : Spek({
    val flow by memoized { TestAtomicStateFlow(0) }

    describe("A test action") {
        val subject by memoized { TestLiveAction() }

        it("FIXME: Not yet implemented") {
            fail("Not yet implemented")
        }
    }
})

private class TestLiveAction : LiveAction<Int>() {
    override suspend fun onLive(stateFlow: AtomicStateFlow<Int>) {
        TODO("Not yet implemented")
    }
}
