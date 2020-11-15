package se.gustavkarlsson.conveyor.rx2

import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Store

object AsRxStoreTest : Spek({
    describe("A asRxStore function") {
        it("invoking works") {
            Store(1).asRxStore()
        }
    }
})
