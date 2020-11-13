package se.gustavkarlsson.conveyor

import kotlinx.coroutines.test.TestCoroutineScope
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThrows

object StartTest : Spek({

    describe("A store") {
        val store by memoized {
            buildStore(0)
        }

        it("starts when invoking start extension function") {
            val scope = TestCoroutineScope()
            scope.start(store)
            expectThrows<StoreAlreadyStartedException> {
                store.start(scope)
            }
        }
    }
})
