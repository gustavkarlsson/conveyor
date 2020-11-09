package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.runBlockingTest
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
                    subject.toList()
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
                    subject.toList()
                }
                subject.update { state1 }
                deferred.cancel()
                deferred.await()
            }
            expectThat(result).containsExactly(initialState, state1)
        }
        it("subscriptionCount is initially 0") {
            val result = runBlockingTest {
                subject.subscriptionCount.first()
            }
            expectThat(result).isEqualTo(0)
        }
        it("subscriptionCount is 1 with 1 collectors") {
            val result = runBlockingTest {
                val job = launch { subject.collect() }
                val subscriptions = subject.subscriptionCount.first()
                job.cancel()
                subscriptions
            }
            expectThat(result).isEqualTo(1)
        }
        it("subscriptionCount is 2 with 2 collectors") {
            val result = runBlockingTest {
                val job1 = launch { subject.collect() }
                val job2 = launch { subject.collect() }
                val subscriptions = subject.subscriptionCount.first()
                job1.cancel()
                job2.cancel()
                subscriptions
            }
            expectThat(result).isEqualTo(2)
        }
    }
})
