package se.gustavkarlsson.cokrate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThrows

@ExperimentalCoroutinesApi
@FlowPreview
object StoreImplTest : Spek({
    describe("Store creation") {
        it("throws exception bufferSize = 0") {
            expectThrows<IllegalArgumentException> {
                StoreImpl(Unit, emptyList(), 0)
            }
        }
        it("throws exception with bufferSize = -1") {
            expectThrows<IllegalArgumentException> {
                StoreImpl(Unit, emptyList(), 0)
            }
        }
    }
    describe("A non-started store") {
        val store by memoized { StoreImpl<State>(State.Initial, emptyList(), 8) }

        it("throws exception when command is issued") {
            runBlocking {
                expectThrows<IllegalStateException> {
                    store.issue { State.Final.only() }
                }
            }
        }

        it("throws exception when command is issued") {
            runBlocking {
                expectThrows<IllegalStateException> {
                    store.issue { State.Final.only() }
                }
            }
        }
    }
    describe("A started store") {
        val scope by memoized {
            CoroutineScope(Dispatchers.Unconfined)
        }
        val store by memoized {
            StoreImpl<State>(State.Initial, emptyList(), 8)
                .apply { start(scope) }
        }
        afterEachTest {
            scope.cancel("Test ended")
        }

        it("throws exception when started") {
            expectThrows<IllegalStateException> {
                store.start(scope)
            }
        }
    }
})

private sealed class State {
    object Initial : State()
    object Final : State()
}
