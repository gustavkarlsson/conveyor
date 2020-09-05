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
import se.gustavkarlsson.conveyor.actions.VoidAction
import se.gustavkarlsson.conveyor.test.FixedStateCommand
import strikt.api.expect
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import strikt.assertions.message

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
                StoreImpl(Unit, commandBufferSize = 0)
            }.message
                .isNotNull()
                .contains(bufferSizeErrorMessage(0))
        }
        it("throws exception with negative command buffer size") {
            expectThrows<IllegalArgumentException> {
                StoreImpl(Unit, commandBufferSize = -1)
            }.message
                .isNotNull()
                .contains(bufferSizeErrorMessage(-1))
        }
    }
    describe("A minimal store") {
        val store by memoized {
            StoreImpl(initialState)
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
                }.message
                    .isNotNull()
                    .contains(STORE_STARTED_ERROR_MESSAGE)
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
                    }.message
                        .isNotNull()
                        .contains(STORE_STARTED_ERROR_MESSAGE)
                }
                it("throws exception when a command is issued") {
                    expectThrows<IllegalStateException> {
                        store.issue { Change("shouldThrow") }
                    }.message
                        .isNotNull()
                        .contains(STORE_STOPPED_ERROR_MESSAGE)
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
            StoreImpl(initialState, initialActions = listOf(action))
        }

        it("the state does not change before starting") {
            expectThat(store.currentState).isEqualTo(initialState)
        }
        it("the state changes when starting") {
            store.start(scope)
            expectThat(store.currentState).isEqualTo(afterCommandState)
        }
    }
    describe("A store with one simple online action") {
        val afterCommandState = "after_command"
        val command = FixedStateCommand(afterCommandState)
        val action = SingleAction { command }
        val store by memoized {
            StoreImpl(initialState, onlineActions = listOf(action))
        }

        it("the state does not change before starting") {
            expectThat(store.currentState).isEqualTo(initialState)
        }
        it("the state does not change when starting") {
            store.start(scope)
            expectThat(store.currentState).isEqualTo(initialState)
        }
        it("the state changes after started and first collector runs") {
            store.start(scope)
            runBlockingTest {
                store.state.first { it == afterCommandState }
            }
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
            StoreImpl(initialState, initialActions = listOf(action))
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
            StoreImpl(initialState, initialActions = listOf(action1, action2))
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
        it("a command changes state immediately") {
            val afterIssuedCommandState = "after_issued_command"
            runBlockingTest {
                store.issue(FixedStateCommand(afterIssuedCommandState))
            }
            expectThat(store.currentState).isEqualTo(afterIssuedCommandState)
            expect {
                that(scope.currentTime).isEqualTo(0)
                that(store.currentState).isEqualTo(afterIssuedCommandState)
            }
        }
        it("a command with a delayed action does not delay initial actions") {
            val command = Command<String> { oldState ->
                Change(oldState, VoidAction { delay(500) })
            }
            runBlockingTest {
                store.issue(command)
            }
            scope.advanceTimeBy(delay1Millis)
            expect {
                that(scope.currentTime).isEqualTo(delay1Millis)
                that(store.currentState).isEqualTo(afterCommand1State)
            }
        }
    }
})
