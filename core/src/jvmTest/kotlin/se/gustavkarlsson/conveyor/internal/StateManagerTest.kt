package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.testing.memoizedTestCoroutineScope
import se.gustavkarlsson.conveyor.testing.runBlockingTest
import strikt.api.expect
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

object StateManagerTest : Spek({
    val scope by memoizedTestCoroutineScope()
    val initialState = "initial"
    val state1 = "state1"
    val state2 = "state2"

    describe("A StateManager") {
        val subject by memoized { StateManager(initialState, emptyList()) }

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
        it("update sets new state after updating it") {
            runBlockingTest {
                subject.update { this + state1 }
            }
            expectThat(subject.value).isEqualTo(initialState + state1)
        }
        it("updateAndGet sets new state after updating it") {
            runBlockingTest {
                subject.updateAndGet { this + state1 }
            }
            expectThat(subject.value).isEqualTo(initialState + state1)
        }
        it("getAndUpdate sets new state after updating it") {
            runBlockingTest {
                subject.getAndUpdate { this + state1 }
            }
            expectThat(subject.value).isEqualTo(initialState + state1)
        }
        it("updateAndGet returns new state after updating it") {
            val result = runBlockingTest {
                subject.updateAndGet { this + state1 }
            }
            expectThat(result).isEqualTo(initialState + state1)
        }
        it("updateAndGet returns old state after updating it") {
            val result = runBlockingTest {
                subject.getAndUpdate { this + state1 }
            }
            expectThat(result).isEqualTo(initialState)
        }
        it("emit sets new state") {
            runBlockingTest {
                subject.emit(state1)
            }
            expectThat(subject.value).isEqualTo(state1)
        }
        it("tryEmit sets new state and returns true") {
            val success = subject.tryEmit(state1)
            expect {
                that(success).isTrue()
                that(subject.value).isEqualTo(state1)
            }
        }
        it("flow emits initial and state1 when updating it to state1 when collecting") {
            val result = runBlockingTest {
                val deferred = async {
                    subject.take(3).toList()
                }
                subject.emit(state1)
                deferred.cancel()
                deferred.await()
            }
            expectThat(result).containsExactly(initialState, state1)
        }
        it("subscriberCount is initially 0") {
            expectThat(subject.subscriptionCount.value).describedAs("current subscriber count")
                .isEqualTo(0)
        }
        it("subscriberCount updates with subscribers") {
            runBlockingTest {
                val job1 = launch {
                    subject.collect {}
                }
                val job2 = launch {
                    subject.collect {}
                }
                expectThat(subject.subscriptionCount.value).describedAs("current subscriber count")
                    .isEqualTo(2)
                job1.cancel()
                job2.cancel()
            }
        }
        it("subscriberCount does not update with subscribers to outgoing internal state") {
            runBlockingTest {
                val job = launch {
                    subject.outgoingState.collect {}
                }
                expectThat(subject.subscriptionCount.value).describedAs("current subscriber count")
                    .isEqualTo(0)
                job.cancel()
            }
        }
        it("storeSubscriberCount is initially 0") {
            expectThat(subject.storeSubscriberCount.value).describedAs("current subscriber count")
                .isEqualTo(0)
        }
        it("storeSubscriberCount updates with subscribers to outgoing state") {
            runBlockingTest {
                val job1 = launch {
                    subject.outgoingState.collect {}
                }
                val job2 = launch {
                    subject.outgoingState.collect {}
                }
                expectThat(subject.storeSubscriberCount.value).describedAs("current subscriber count")
                    .isEqualTo(2)
                job1.cancel()
                job2.cancel()
            }
        }
        it("storeSubscriberCount does not update with subscribers to internal state") {
            runBlockingTest {
                val job = launch {
                    subject.collect {}
                }
                expectThat(subject.storeSubscriberCount.value).describedAs("current subscriber count")
                    .isEqualTo(0)
                job.cancel()
            }
        }
        it("slow collector of outgoingState does not miss any emissions") {
            val collected = mutableListOf<String>()
            scope.launch { subject.run() }
            scope.runBlockingTest {
                val job = launch {
                    subject.outgoingState
                        .onEach { delay(1) }
                        .toCollection(collected)
                }
                subject.tryEmit("first")
                subject.tryEmit("second")
                advanceTimeBy(3)
                job.cancel()
            }
            expectThat(collected).containsExactly("initial", "first", "second")
        }
    }
    describe("A StateManager with transformers") {
        val addIndex: Transformer<String> = { flow ->
            flow.withIndex()
                .map { "${it.value}-${it.index}" }
        }
        val dropOdd: Transformer<String> = { flow ->
            flow.filter {
                val index = it.substringAfter("-").toInt()
                index % 2 == 0
            }
        }
        val subject by memoized { StateManager(initialState, listOf(addIndex, dropOdd)) }

        it("transformers run when run") {
            val result = mutableListOf<String>()
            runBlockingTest {
                val runJob = launch { subject.run() }
                val collectJob = launch { subject.outgoingState.toCollection(result) }

                subject.tryEmit("first")
                subject.tryEmit("second")

                runJob.cancel()
                collectJob.cancel()
            }
            expectThat(result).containsExactly("initial-0", "second-2")
        }
    }
    describe("A StateManager with a delaying transformer") {
        val delay: Transformer<String> = { flow ->
            flow.onEach { delay(10) }
        }
        val subject by memoized { StateManager(initialState, listOf(delay)) }
        beforeEachTest {
            scope.launch { subject.run() }
        }

        it("suspends emissions due to backpressure") {
            expectThrows<TimeoutCancellationException> {
                withTimeout(15) {
                    subject.emit("first")
                    subject.emit("second")
                }
            }
        }
        it("does not miss any emissions") {
            val values = mutableListOf<String>()
            scope.runBlockingTest {
                launch {
                    subject.emit("first")
                }
                launch {
                    subject.emit("second")
                }
                values += subject.value
                advanceTimeBy(10)
                values += subject.value
            }
            expectThat(values).containsExactly("first", "second")
        }
        it("tryEmit fails to set new state and returns false when blocked") {
            subject.tryEmit(state1)
            val success = subject.tryEmit(state2)
            expect {
                that(success).isFalse()
                that(subject.value).isEqualTo(state1)
            }
        }
    }
})
