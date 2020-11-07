package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.runBlockingTest
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

object StateManagerTest : Spek({
    val initialState = "initial"
    val state1 = "state1"
    val state2 = "state2"

    describe("A minimal manager") {
        val subject by memoized { StateManager(initialState) }

        it("state.value returns initial") {
            expectThat(subject.state.value).isEqualTo(initialState)
        }
        it("flow emits initial") {
            val result = runBlockingTest {
                val deferred = async {
                    subject.state.toList()
                }
                deferred.cancel()
                deferred.await()
            }
            expectThat(result).containsExactly(initialState)
        }
        it("state.value returns state1 after setting it to state1") {
            runBlockingTest {
                subject.set(state1)
            }
            expectThat(subject.state.value).isEqualTo(state1)
        }
        it("state.value returns new state after updating it") {
            runBlockingTest {
                subject.update { it + state1 }
            }
            expectThat(subject.state.value).isEqualTo(initialState + state1)
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
            expectThat(subject.state.value).isEqualTo(state2)
        }
        it("delayed update takes a while") {
            runBlockingTest {
                launch {
                    subject.update {
                        delay(1)
                        state1
                    }
                }
                expectThat(subject.state.value).isEqualTo(initialState)
                advanceTimeBy(1)
                expectThat(subject.state.value).isEqualTo(state1)
            }
        }
        it("flow emits initial and state1 when updating it to state1 when collecting") {
            val result = runBlockingTest {
                val deferred = async {
                    subject.state.toList()
                }
                subject.update { state1 }
                deferred.cancel()
                deferred.await()
            }
            expectThat(result).containsExactly(initialState, state1)
        }
    }
})
