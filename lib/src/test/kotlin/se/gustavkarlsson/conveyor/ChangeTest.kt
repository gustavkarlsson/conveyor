package se.gustavkarlsson.conveyor

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.NullAction
import strikt.api.expectThat
import strikt.assertions.containsExactly

object ChangeTest : Spek({
    describe("Change creation") {
        it("vararg constructor creates list of actions") {
            val action1 = NullAction<String>()
            val action2 = NullAction<String>()
            val change = Change("state", action1, action2)
            expectThat(change.actions).containsExactly(action1, action2)
        }
    }
})
