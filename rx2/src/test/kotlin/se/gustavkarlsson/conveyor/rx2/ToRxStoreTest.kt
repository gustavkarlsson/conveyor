package se.gustavkarlsson.conveyor.rx2

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.buildStore

object ToRxStoreTest : Spek({
    describe("A toRxStore function") {
        it("invoking works") {
            buildStore(1).toRxStore()
        }
    }
})
