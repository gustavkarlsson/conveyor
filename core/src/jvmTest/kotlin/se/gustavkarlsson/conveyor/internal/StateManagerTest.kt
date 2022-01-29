package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.StateUpdateException
import strikt.api.expect
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.cause
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

object StateManagerTest : Spek({
    val errorMessage = "failed"
    val initialState = "initial"
    val state1 = "state1"
    val state2 = "state2"

    describe("A StateManager") {
        val subject by memoized { StateManager(initialState, emptyList()) }

        it("value returns initial") {
            expectThat(subject.value).isEqualTo(initialState)
        }
        it("flow emits initial") {
            runTest {
                val deferred = async {
                    subject.take(2).toList()
                }
                deferred.cancel()
                val result = deferred.await()
                expectThat(result).containsExactly(initialState)
            }
        }
        it("update sets new state after updating it") {
            runTest {
                subject.update { this + state1 }
            }
            expectThat(subject.value).isEqualTo(initialState + state1)
        }
        it("updateAndGet sets new state after updating it") {
            runTest {
                subject.updateAndGet { this + state1 }
            }
            expectThat(subject.value).isEqualTo(initialState + state1)
        }
        it("getAndUpdate sets new state after updating it") {
            runTest {
                subject.getAndUpdate { this + state1 }
            }
            expectThat(subject.value).isEqualTo(initialState + state1)
        }
        it("updateAndGet returns new state after updating it") {
            runTest {
                val result = subject.updateAndGet { this + state1 }
                expectThat(result).isEqualTo(initialState + state1)
            }
        }
        it("updateAndGet returns old state after updating it") {
            runTest {
                val result = subject.getAndUpdate { this + state1 }
                expectThat(result).isEqualTo(initialState)
            }
        }
        it("emit sets new state") {
            runTest {
                subject.emit(state1)
                expectThat(subject.value).isEqualTo(state1)
            }
        }
        it("tryEmit sets new state and returns true") {
            val success = subject.tryEmit(state1)
            expect {
                that(success).isTrue()
                that(subject.value).isEqualTo(state1)
            }
        }
        it("flow emits initial and state1 when updating it to state1 when collecting") {
            runTest {
                val deferred = async {
                    subject.take(3).toList()
                }
                subject.emit(state1)
                deferred.cancel()
                val result = deferred.await()
                expectThat(result).containsExactly(initialState, state1)
            }
        }
        it("subscriberCount is initially 0") {
            expectThat(subject.subscriptionCount.value).describedAs("current subscriber count")
                .isEqualTo(0)
        }
        it("subscriberCount updates with subscribers") {
            runTest {
                launch {
                    subject.collect {}
                }
                launch {
                    subject.collect {}
                }
                runCurrent()
                expectThat(subject.subscriptionCount.value).describedAs("current subscriber count")
                    .isEqualTo(2)
                cancel()
            }
        }
        it("subscriberCount does not update with subscribers to outgoing internal state") {
            runTest {
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
            runTest {
                launch {
                    subject.outgoingState.collect {}
                }
                launch {
                    subject.outgoingState.collect {}
                }
                runCurrent()
                expectThat(subject.storeSubscriberCount.value).describedAs("current subscriber count")
                    .isEqualTo(2)
                cancel()
            }
        }
        it("storeSubscriberCount does not update with subscribers to internal state") {
            runTest {
                val job = launch {
                    subject.collect {}
                }
                expectThat(subject.storeSubscriberCount.value).describedAs("current subscriber count")
                    .isEqualTo(0)
                job.cancel()
            }
        }
        it("slow collector of outgoingState does not miss any emissions") {
            runTest {
                val collected = mutableListOf<String>()
                launch { subject.run() }
                launch {
                    subject.outgoingState
                        .onEach { delay(1) }
                        .toCollection(collected)
                }
                runCurrent()
                subject.tryEmit("first")
                runCurrent()
                subject.tryEmit("second")
                advanceTimeBy(100)
                expectThat(collected).containsExactly("initial", "first", "second")
                cancel()
            }
        }
        it("throws exception containing state when update fails") {
            expectThrows<StateUpdateException> {
                subject.update {
                    error(errorMessage)
                }
            }.and {
                get { state }.describedAs("state")
                    .isEqualTo(initialState)
                cause
                    .get { this?.message }.describedAs("message")
                    .isEqualTo(errorMessage)
            }
        }
        it("throws exception containing state when updateAndGet fails") {
            expectThrows<StateUpdateException> {
                subject.updateAndGet {
                    error(errorMessage)
                }
            }.and {
                get { state }.describedAs("state")
                    .isEqualTo(initialState)
                cause
                    .get { this?.message }.describedAs("message")
                    .isEqualTo(errorMessage)
            }
        }
        it("throws exception containing state when getAndUpdate fails") {
            expectThrows<StateUpdateException> {
                subject.getAndUpdate {
                    error(errorMessage)
                }
            }.and {
                get { state }.describedAs("state")
                    .isEqualTo(initialState)
                cause
                    .get { this?.message }.describedAs("message")
                    .isEqualTo(errorMessage)
            }
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
            runTest {
                launch { subject.run() }
                launch { subject.outgoingState.toCollection(result) }
                runCurrent()

                subject.tryEmit("first")
                subject.tryEmit("second")

                cancel()
            }
            expectThat(result).containsExactly("initial-0", "second-2")
        }
    }
    describe("A StateManager with a delaying transformer") {
        val delay: Transformer<String> = { flow ->
            flow.onEach { delay(10) }
        }
        val subject by memoized { StateManager(initialState, listOf(delay)) }

        it("suspends emissions due to backpressure") {
            runTest {
                launch { subject.run() }
                runCurrent()
                expectThrows<TimeoutCancellationException> {
                    withTimeout(15) {
                        subject.emit("first")
                        subject.emit("second")
                    }
                }
                cancel()
            }
        }
        it("does not miss any emissions") {
            runTest {
                val values = mutableListOf<String>()
                launch { subject.run() }
                launch {
                    subject.emit("first")
                }
                launch {
                    subject.emit("second")
                }
                runCurrent()
                values += subject.value
                advanceTimeBy(100)
                values += subject.value
                expectThat(values).containsExactly("first", "second")
                cancel()
            }
        }
        it("tryEmit fails to set new state and returns false when blocked") {
            runTest {
                launch { subject.run() }
                runCurrent()
                subject.tryEmit(state1)
                val success = subject.tryEmit(state2)
                expect {
                    that(success).isFalse()
                    that(subject.value).isEqualTo(state1)
                }
                cancel()
            }
        }
    }
})
