package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

object StateManagerTest : Spek({
    val initialState = "initial"
    val state1 = "state1"
    val state2 = "state2"

    describe("A minimal manager") {
        val subject by memoized { StateManager(initialState) }

        it("get returns initial") {
            expectThat(subject.get()).isEqualTo(initialState)
        }
        it("flow emits initial") {
            val result = runBlockingTest {
                val deferred = async {
                    subject.flow.toList()
                }
                deferred.cancel()
                deferred.await()
            }
            expectThat(result).containsExactly(initialState)
        }
        it("get returns state1 after setting it to state1") {
            runBlockingTest {
                subject.set(state1)
            }
            expectThat(subject.get()).isEqualTo(state1)
        }
        it("get returns new state after updating it") {
            runBlockingTest {
                subject.update { it + state1 }
            }
            expectThat(subject.get()).isEqualTo(initialState + state1)
        }
        it("update and set runs sequentially") {
            runBlockingTest {
                launch {
                    subject.update {
                        delay(1)
                        state1
                    }
                }
                subject.set(state2)
                advanceTimeBy(1)
            }
            expectThat(subject.get()).isEqualTo(state2)
        }
        it("delayed update takes a while") {
            runBlockingTest {
                launch {
                    subject.update {
                        delay(1)
                        state1
                    }
                }
                expectThat(subject.get()).isEqualTo(initialState)
                advanceTimeBy(1)
                expectThat(subject.get()).isEqualTo(state1)
            }
        }
        it("flow emits initial and state1 when updating it to state1 when collecting") {
            val result = runBlockingTest {
                val deferred = async {
                    subject.flow.toList()
                }
                subject.update { state1 }
                deferred.cancel()
                deferred.await()
            }
            expectThat(result).containsExactly(initialState, state1)
        }
        it("flow emits initial and ends after cancelling") {
            val result = runBlockingTest {
                val deferred = async {
                    subject.flow.single()
                }
                subject.cancel()
                deferred.await()
            }
            expectThat(result).isEqualTo(initialState)
        }

        describe("that was cancelled") {
            beforeEachTest {
                subject.cancel()
            }

            it("get returns current state") {
                val result = subject.get()
                expectThat(result).isEqualTo(initialState)
            }
            it("flow emits current state and then completes") {
                val result = runBlockingTest {
                    subject.flow.single()
                }
                expectThat(result).isEqualTo(initialState)
            }
            it("set throws") {
                expectThrows<IllegalStateException> {
                    runBlockingTest {
                        subject.update { "shouldThrow" }
                    }
                }
            }
            it("update throws") {
                expectThrows<IllegalStateException> {
                    runBlockingTest {
                        subject.update { "shouldThrow" }
                    }
                }
            }
            it("get after trying to set state returns old state") {
                try {
                    runBlockingTest {
                        subject.set("shouldThrow")
                    }
                } catch (ignore: Throwable) {
                }
                expectThat(subject.get()).isEqualTo(initialState)
            }
            it("get after trying to update state returns old state") {
                try {
                    runBlockingTest {
                        subject.update { "shouldThrow" }
                    }
                } catch (ignore: Throwable) {
                }
                expectThat(subject.get()).isEqualTo(initialState)
            }
            it("flow emits current state and then ends") {
                val result = runBlockingTest {
                    subject.flow.single()
                }
                expectThat(result).isEqualTo(initialState)
            }
            it("cancel is successful") {
                subject.cancel()
            }
        }
    }
})
