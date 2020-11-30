package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.testing.runBlockingTest
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

object UpdatableStateFlowImplTest : Spek({
    val initialState = "initial"
    val state1 = "state1"
    val state2 = "state2"

    describe("An UpdatableStateFlowImpl") {
        val subject by memoized { UpdatableStateFlowImpl(initialState) }

        it("value returns initial") {
            expectThat(subject.value).isEqualTo(initialState)
        }
        it("flow emits initial") {
            val result = runBlockingTest {
                val deferred = async {
                    subject.take(2).toList()
                }
                deferred.cancel()
                deferred.await()
            }
            expectThat(result).containsExactly(initialState)
        }
        it("value returns new state after updating it") {
            runBlockingTest {
                subject.update { this + state1 }
            }
            expectThat(subject.value).isEqualTo(initialState + state1)
        }
        it("update twice runs sequentially") {
            runBlockingTest {
                launch {
                    subject.update {
                        delay(1)
                        state1
                    }
                }
                launch {
                    subject.update { state2 }
                }
                advanceTimeBy(1)
            }
            expectThat(subject.value).isEqualTo(state2)
        }
        it("delayed update takes a while") {
            runBlockingTest {
                launch {
                    subject.update {
                        delay(1)
                        state1
                    }
                }
                expectThat(subject.value).isEqualTo(initialState)
                advanceTimeBy(1)
                expectThat(subject.value).isEqualTo(state1)
            }
        }
        it("flow emits initial and state1 when updating it to state1 when collecting") {
            val result = runBlockingTest {
                val deferred = async {
                    subject.take(3).toList()
                }
                subject.update { state1 }
                deferred.cancel()
                deferred.await()
            }
            expectThat(result).containsExactly(initialState, state1)
        }
    }
})
