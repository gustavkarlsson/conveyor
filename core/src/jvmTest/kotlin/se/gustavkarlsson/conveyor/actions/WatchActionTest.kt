package se.gustavkarlsson.conveyor.actions

import org.junit.jupiter.api.fail
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.testing.TestAtomicStateFlow

object WatchActionTest : Spek({
    val flow by memoized { TestAtomicStateFlow(0) }

    describe("A test action") {
        val subject by memoized { TestWatchAction() }

        it("FIXME: Not yet implemented") {
            fail("Not yet implemented")
        }
    }
})

private class TestWatchAction : WatchAction<Int>() {
    private val _watched = mutableListOf<Int>()
    val watched: List<Int> = _watched

    override fun onState(state: Int) {
        _watched += state
    }
}
