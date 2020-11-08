package se.gustavkarlsson.conveyor

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.SetStateAction
import se.gustavkarlsson.conveyor.test.memoizedTestCoroutineScope
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expect
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

object StoreIntegrationTest : Spek({
    val initialState = "initial"
    val state1 = "state1"
    val state2 = "state2"
    val fixedStateAction1 = SetStateAction(state1)
    val delay5 = 5L
    val delay10 = 10L
    val delay20 = 20L
    val delayAction5 = action<String> {
        delay(delay5)
    }
    val delayAction10 = action<String> { stateAccess ->
        delay(delay10)
        stateAccess.update { state1 }
    }
    val delayAction20 = action<String> { stateAccess ->
        delay(delay20)
        stateAccess.update { state2 }
    }
    val scope by memoizedTestCoroutineScope()

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
        it("state.value returns initial") {
            val result = subject.state.value
            expectThat(result).isEqualTo(initialState)
        }
        it("throws when issuing action") {
            expectThrows<StoreNotYetStartedException> {
                subject.issue(fixedStateAction1)
            }
        }

        describe("that was started") {
            lateinit var job: Job
            beforeEachTest {
                job = subject.start(scope)
            }

            it("has an active job") {
                expectThat(job.isActive).isTrue()
            }
            it("throws exception when started") {
                expectThrows<StoreAlreadyStartedException> {
                    subject.start(scope)
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

            describe("and had its job explicitly cancelled") {
                beforeEachTest {
                    job.cancel("Purposefully cancelled before test")
                    scope.advanceUntilIdle() // TODO figure one why this is necessary
                }

                it("throws exception when started") {
                    expectThrows<StoreStoppedException> {
                        subject.start(scope)
                    }
                }
                it("throws exception when an action is issued") {
                    expectThrows<StoreStoppedException> {
                        subject.issue(action {})
                    }
                }
                it("state.value returns initial") {
                    val result = subject.state.value
                    expectThat(result).isEqualTo(initialState)
                }
                it("state emits initial") {
                    val result = runBlockingTest {
                        subject.state.first()
                    }
                    expectThat(result).isEqualTo(initialState)
                }
            }
        }
    }
    describe("A store with one simple start action") {
        val store by memoized {
            buildStore(initialState, startActions = listOf(fixedStateAction1))
        }

        it("the state does not change before starting") {
            expectThat(store.state.value).isEqualTo(initialState)
        }
        it("the state changes when starting") {
            store.start(scope)
            expectThat(store.state.value).isEqualTo(state1)
        }
    }
    describe("A store with one simple live action") {
        val store by memoized {
            buildStore(initialState, liveActions = listOf(fixedStateAction1))
        }

        it("the state does not change before starting") {
            expectThat(store.state.value).isEqualTo(initialState)
        }
        it("the state does not change when starting") {
            store.start(scope)
            expectThat(store.state.value).isEqualTo(initialState)
        }
        it("the state changes after started and first collector runs") {
            store.start(scope)
            scope.advanceUntilIdle() // TODO Figure out why this is necessary
            val result = runBlockingTest {
                store.state.first()
            }
            expectThat(result).isEqualTo(state1)
        }
    }
    describe("A started store with one delayed start action") {
        val store by memoized {
            buildStore(initialState, startActions = listOf(delayAction10))
        }
        beforeEachTest {
            store.start(scope)
        }

        it("the state does not change immediately") {
            expectThat(store.state.value).isEqualTo(initialState)
        }
        it("the state changes after the delay has passed") {
            scope.advanceTimeBy(delay10)
            expectThat(store.state.value).isEqualTo(state1)
        }
        it("the state does not change if its scope was cancelled before the delay has passed") {
            scope.cancel("Purposefully cancelled")
            scope.advanceTimeBy(delay10)
            expectThat(store.state.value).isEqualTo(initialState)
        }
    }
    describe("A started store with two delayed start actions") {
        val store by memoized {
            buildStore(initialState, startActions = listOf(delayAction10, delayAction20))
        }
        beforeEachTest {
            store.start(scope)
        }

        it("the state changes after the first delay has passed") {
            scope.advanceTimeBy(delay10)
            expectThat(store.state.value).isEqualTo(state1)
        }
        it("the state changes after the second delay has passed") {
            scope.advanceTimeBy(delay20)
            expectThat(store.state.value).isEqualTo(state2)
        }
        it("an action changes state immediately") {
            store.issue(fixedStateAction1)
            expectThat(store.state.value).isEqualTo(state1)
            expect {
                that(scope.currentTime).isEqualTo(0)
                that(store.state.value).isEqualTo(state1)
            }
        }
        it("an action with a delayed action does not delay start actions") {
            store.issue(delayAction5)
            scope.advanceTimeBy(delay10)
            expect {
                that(scope.currentTime).isEqualTo(delay10)
                that(store.state.value).isEqualTo(state1)
            }
        }
    }
})
