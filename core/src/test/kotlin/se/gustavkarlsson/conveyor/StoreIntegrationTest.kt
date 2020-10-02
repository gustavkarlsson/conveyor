package se.gustavkarlsson.conveyor

import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestCoroutineScope
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.SetStateAction
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expect
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.contains
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import strikt.assertions.message

object StoreIntegrationTest : Spek({
    val initialState = "initial"
    val state1 = "state1"
    val state2 = "state2"
    val fixedStateAction1 = SetStateAction(state1)
    val delay5 = 5L
    val delay10 = 10L
    val delay20 = 20L
    val delayAction5 = Action<String> {
        delay(delay5)
    }
    val delayAction10 = Action<String> { updateState ->
        delay(delay10)
        updateState { state1 }
    }
    val delayAction20 = Action<String> { updateState ->
        delay(delay20)
        updateState { state2 }
    }
    val scope by memoized(
        factory = { TestCoroutineScope(Job()) },
        destructor = {
            it.cancel("Test ended")
            it.cleanupTestCoroutines()
        }
    )

    describe("Store creation") {
        it("throws exception with empty action buffer") {
            expectThrows<IllegalArgumentException> {
                buildStore(Unit, actionBufferSize = 0)
            }.message
                .isNotNull()
                .contains("positive")
        }
        it("throws exception with negative action buffer size") {
            expectThrows<IllegalArgumentException> {
                buildStore(Unit, actionBufferSize = -1)
            }.message
                .isNotNull()
                .contains("positive")
        }
    }
    describe("A minimal store") {
        val subject by memoized {
            buildStore(initialState)
        }

        it("state emits initial") {
            val result = runBlockingTest {
                subject.state.first()
            }
            expectThat(result).isEqualTo(initialState)
        }
        it("currentState returns initial") {
            val result = subject.currentState
            expectThat(result).isEqualTo(initialState)
        }
        it("state emits initial after issuing action that changes state") {
            val result = runBlockingTest {
                subject.issue(fixedStateAction1)
                subject.state.first()
            }
            expectThat(result).isEqualTo(initialState)
        }
        it("currentState returns initial even after issuing action") {
            runBlockingTest {
                subject.issue(fixedStateAction1)
            }
            expectThat(subject.currentState).isEqualTo(initialState)
        }
        it("state emits initial and new state when issuing action that changes state and then starting") {
            val result = runBlockingTest {
                val deferred = async { subject.state.take(2).toList() }
                subject.issue(fixedStateAction1)
                subject.open(scope)
                deferred.await()
            }
            expectThat(result).containsExactly(initialState, state1)
        }
        it("currentState returns new state after issuing action that changes state and then starting") {
            runBlockingTest {
                subject.issue(fixedStateAction1)
            }
            subject.open(scope)
            expectThat(subject.currentState).isEqualTo(state1)
        }

        describe("that was started") {
            lateinit var job: Job
            beforeEachTest {
                job = subject.open(scope)
            }

            it("has an active job") {
                expectThat(job.isActive).isTrue()
            }
            it("throws exception when started") {
                expectThrows<StoreOpenedException> {
                    subject.open(scope)
                }
            }
            it("has its job cancelled after its scope was cancelled") {
                scope.cancel("Cancelling scope to test job cancellation")
                expectThat(job.isCancelled).isTrue()
            }
            it("state emits initial") {
                val result = runBlockingTest {
                    subject.state.first()
                }
                expectThat(result).isEqualTo(initialState)
            }
            it("existing state subscription ends when job is cancelled") {
                val result = runBlockingTest {
                    val deferred = async { subject.state.toList() }
                    job.cancel("Purposefully cancelled")
                    deferred.await()
                }
                expectThat(result).containsExactly(initialState)
            }

            describe("and had its job explicitly cancelled") {
                beforeEachTest {
                    job.cancel("Purposefully cancelled before test")
                }

                it("throws exception when started") {
                    expectThrows<StoreClosedException> {
                        subject.open(scope)
                    }
                }
                it("throws exception when an action is issued") {
                    expectThrows<StoreClosedException> {
                        subject.issue(Action {})
                    }
                }
                it("currentState returns initial") {
                    val result = subject.currentState
                    expectThat(result).isEqualTo(initialState)
                }
                it("state emits initial and then closes") {
                    val result = runBlockingTest {
                        subject.state.toList()
                    }
                    expectThat(result).containsExactly(initialState)
                }
            }
        }
    }
    describe("A store with one simple start action") {
        val store by memoized {
            buildStore(initialState, openActions = listOf(fixedStateAction1))
        }

        it("the state does not change before starting") {
            expectThat(store.currentState).isEqualTo(initialState)
        }
        it("the state changes when starting") {
            store.open(scope)
            expectThat(store.currentState).isEqualTo(state1)
        }
    }
    describe("A store with one simple live action") {
        val store by memoized {
            buildStore(initialState, liveActions = listOf(fixedStateAction1))
        }

        it("the state does not change before starting") {
            expectThat(store.currentState).isEqualTo(initialState)
        }
        it("the state does not change when starting") {
            store.open(scope)
            expectThat(store.currentState).isEqualTo(initialState)
        }
        it("the state changes after started and first collector runs") {
            store.open(scope)
            runBlockingTest {
                store.state.first { it == state1 }
            }
        }
    }
    describe("A started store with one delayed start action") {
        val store by memoized {
            buildStore(initialState, openActions = listOf(delayAction10))
        }
        beforeEachTest {
            store.open(scope)
        }

        it("the state does not change immediately") {
            expectThat(store.currentState).isEqualTo(initialState)
        }
        it("the state changes after the delay has passed") {
            scope.advanceTimeBy(delay10)
            expectThat(store.currentState).isEqualTo(state1)
        }
        it("the state does not change if its scope was cancelled before the delay has passed") {
            scope.cancel("Purposefully cancelled")
            scope.advanceTimeBy(delay10)
            expectThat(store.currentState).isEqualTo(initialState)
        }
    }
    describe("A started store with two delayed start actions") {
        val store by memoized {
            buildStore(initialState, openActions = listOf(delayAction10, delayAction20))
        }
        beforeEachTest {
            store.open(scope)
        }

        it("the state changes after the first delay has passed") {
            scope.advanceTimeBy(delay10)
            expectThat(store.currentState).isEqualTo(state1)
        }
        it("the state changes after the second delay has passed") {
            scope.advanceTimeBy(delay20)
            expectThat(store.currentState).isEqualTo(state2)
        }
        it("an action changes state immediately") {
            runBlockingTest {
                store.issue(fixedStateAction1)
            }
            expectThat(store.currentState).isEqualTo(state1)
            expect {
                that(scope.currentTime).isEqualTo(0)
                that(store.currentState).isEqualTo(state1)
            }
        }
        it("an action with a delayed action does not delay start actions") {
            runBlockingTest {
                store.issue(delayAction5)
            }
            scope.advanceTimeBy(delay10)
            expect {
                that(scope.currentTime).isEqualTo(delay10)
                that(store.currentState).isEqualTo(state1)
            }
        }
    }
})
