package se.gustavkarlsson.conveyor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.actions.SingleAction
import se.gustavkarlsson.conveyor.test.FixedStateCommand
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

@ExperimentalCoroutinesApi
@FlowPreview
object StoreImplTest : Spek({
    val initialState = "initial"
    val scope by memoized(
        factory = { TestCoroutineScope(Job()) },
        destructor = {
            it.cancel("Test ended")
            it.cleanupTestCoroutines()
        }
    )
    describe("Store creation") {
        it("throws exception with empty command buffer") {
            expectThrows<IllegalArgumentException> {
                StoreImpl(Unit, emptyList(), 0)
            }
        }
        it("throws exception with negative command buffer size") {
            expectThrows<IllegalArgumentException> {
                StoreImpl(Unit, emptyList(), -1)
            }
        }
    }
    describe("A minimal store") {
        val store by memoized {
            StoreImpl(initialState, emptyList(), 8)
        }

        it("throws exception when command is issued") {
            runBlockingTest {
                expectThrows<IllegalStateException> {
                    store.issue { "shouldThrow".only() }
                }
            }
        }
        it("state emits initial") {
            runBlockingTest {
                val result = store.state.first()
                expectThat(result).isEqualTo(initialState)
            }
        }
        it("currentState returns initial") {
            val result = store.currentState
            expectThat(result).isEqualTo(initialState)
        }

        describe("that was started") {
            lateinit var job: Job
            beforeEachTest {
                job = store.start(scope)
            }
            afterEachTest { }

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
                runBlockingTest {
                    val result = store.state.first()
                    expectThat(result).isEqualTo(initialState)
                }
            }
            it("existing state subscription ends when job is cancelled") {
                runBlockingTest {
                    val deferred = async { store.state.toList() }
                    job.cancel("Purposefully cancelled")
                    val result = deferred.await()
                    expectThat(result).containsExactly(initialState)
                }
            }

            describe("and had its job explicitly cancelled") {
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
                        store.issue { "shouldThrow".only() }
                    }
                }
                it("currentState returns initial") {
                    val result = store.currentState
                    expectThat(result).isEqualTo(initialState)
                }
                it("state emits initial and then closes") {
                    runBlockingTest {
                        val result = store.state.toList()
                        expectThat(result).containsExactly(initialState)
                    }
                }
            }
        }
    }
    describe("A store with one simple initial action") {
        val afterCommandState = "after_command"
        val command = FixedStateCommand(afterCommandState)
        val action = SingleAction { command }
        val store by memoized {
            StoreImpl(initialState, listOf(action), 8)
        }

        it("the state does not change before starting") {
            expectThat(store.currentState).isEqualTo(initialState)
        }
        it("the state changes when starting") {
            store.start(scope)
            expectThat(store.currentState).isEqualTo(afterCommandState)
        }
    }
    describe("A started store with one delayed initial action") {
        val afterCommandState = "after_command"
        val command = FixedStateCommand(afterCommandState)
        val delayMillis = 1000L
        val action = SingleAction {
            delay(delayMillis)
            command
        }
        val store by memoized {
            StoreImpl(initialState, listOf(action), 8)
        }
        beforeEachTest {
            store.start(scope)
        }

        it("the state does not change immediately") {
            expectThat(store.currentState).isEqualTo(initialState)
        }
        it("the state changes after the delay has passed") {
            scope.advanceTimeBy(delayMillis)
            expectThat(store.currentState).isEqualTo(afterCommandState)
        }
        it("the state does not change if its scope was cancelled before the delay has passed") {
            scope.cancel("Purposefully cancelled")
            scope.advanceTimeBy(delayMillis)
            expectThat(store.currentState).isEqualTo(initialState)
        }
    }
    describe("A started store with two initial delayed actions") {
        val afterCommand1State = "after_command_1"
        val command1 = FixedStateCommand(afterCommand1State)
        val delay1Millis = 1000L
        val action1 = SingleAction {
            delay(delay1Millis)
            command1
        }
        val afterCommand2State = "after_command_2"
        val command2 = FixedStateCommand(afterCommand2State)
        val delay2Millis = 2000L
        val action2 = SingleAction {
            delay(delay2Millis)
            command2
        }
        val store by memoized {
            StoreImpl(initialState, listOf(action1, action2), 8)
        }
        beforeEachTest {
            store.start(scope)
        }

        it("the state changes after the first delay has passed") {
            scope.advanceTimeBy(delay1Millis)
            expectThat(store.currentState).isEqualTo(afterCommand1State)
        }
        it("the state changes after the second delay has passed") {
            scope.advanceTimeBy(delay2Millis)
            expectThat(store.currentState).isEqualTo(afterCommand2State)
        }
    }
})
