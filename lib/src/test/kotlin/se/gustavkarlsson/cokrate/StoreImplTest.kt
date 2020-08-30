package se.gustavkarlsson.cokrate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

@ExperimentalCoroutinesApi
@FlowPreview
object StoreImplTest : Spek({
    describe("Store creation") {
        it("throws exception with no buffer") {
            expectThrows<IllegalArgumentException> {
                StoreImpl(Unit, emptyList(), 0)
            }
        }
        it("throws exception with negative buffer") {
            expectThrows<IllegalArgumentException> {
                StoreImpl(Unit, emptyList(), 0)
            }
        }
    }
    describe("A minimal non-started store") {
        val initialState = "initial"
        val store by memoized {
            StoreImpl(initialState, emptyList(), 8)
        }

        it("throws exception when command is issued") {
            runBlocking {
                expectThrows<IllegalStateException> {
                    store.issue { "shouldThrow".toChange() }
                }
            }
        }
        it("state emits initial") {
            val result = runBlocking { store.state.first() }
            expectThat(result).isEqualTo(initialState)
        }
    }
    describe("A minimal started store") {
        val initialState = "initial"
        val scope by memoized(
            factory = { CoroutineScope(SupervisorJob() + Dispatchers.Unconfined) },
            destructor = { it.cancel("Test ended") }
        )
        val store by memoized {
            StoreImpl(initialState, emptyList(), 8)
        }
        lateinit var job: Job
        beforeEachTest {
            job = store.start(scope)
        }

        it("has an active job") {
            expectThat(job.isActive).isTrue()
        }
        it("throws exception when started") {
            expectThrows<IllegalStateException> {
                store.start(scope)
            }
        }
        it("has its job cancelled after its scope was cancelled") {
            scope.cancel("Cancelling scope to test job cancellation")
            expectThat(job.isCancelled).isTrue()
        }
        it("state emits initial") {
            val result = runBlocking { store.state.first() }
            expectThat(result).isEqualTo(initialState)
        }

        describe("that had its job explicitly cancelled") {
            beforeEachTest {
                job.cancel("Purposefully cancelled before test")
            }

            it("throws exception when started") {
                expectThrows<IllegalStateException> {
                    store.start(scope)
                }
            }
            it("throws exception when a command is issued") {
                expectThrows<IllegalStateException> {
                    store.issue { "shouldThrow".toChange() }
                }
            }
            it("state emits initial") {
                val result = runBlocking { store.state.first() }
                expectThat(result).isEqualTo(initialState)
            }
        }
    }
})
