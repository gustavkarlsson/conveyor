package se.gustavkarlsson.conveyor.internal

import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import se.gustavkarlsson.conveyor.StateUpdateException
import strikt.api.expect
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.cause
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class StateManagerTest : FunSpec({
    val errorMessage = "failed"
    val initialState = "initial"
    val state1 = "state1"
    val state2 = "state2"
    val subject = StateManager(initialState, emptyList())

    val delayTransformer: Transformer<String> = { flow ->
        flow.onEach { delay(10) }
    }
    val delayedSubject = StateManager(initialState, listOf(delayTransformer))

    test("value returns initial") {
        expectThat(subject.value).isEqualTo(initialState)
    }

    test("flow emits initial") {
        runTest {
            val collected = mutableListOf<String>()
            val collectJob = launch {
                subject
                    .take(2)
                    .toCollection(collected)
            }
            runCurrent()
            collectJob.cancel()
            expectThat(collected).containsExactly(initialState)
        }
    }

    test("update sets new state after updating it") {
        runTest {
            subject.update { this + state1 }
        }
        expectThat(subject.value).isEqualTo(initialState + state1)
    }

    test("updateAndGet sets new state after updating it") {
        runTest {
            subject.updateAndGet { this + state1 }
        }
        expectThat(subject.value).isEqualTo(initialState + state1)
    }

    test("getAndUpdate sets new state after updating it") {
        runTest {
            subject.getAndUpdate { this + state1 }
        }
        expectThat(subject.value).isEqualTo(initialState + state1)
    }

    test("updateAndGet returns new state after updating it") {
        runTest {
            val result = subject.updateAndGet { this + state1 }
            expectThat(result).isEqualTo(initialState + state1)
        }
    }

    test("updateAndGet returns old state after updating it") {
        runTest {
            val result = subject.getAndUpdate { this + state1 }
            expectThat(result).isEqualTo(initialState)
        }
    }

    test("emit sets new state") {
        runTest {
            subject.emit(state1)
            expectThat(subject.value).isEqualTo(state1)
        }
    }

    test("tryEmit sets new state and returns true") {
        val success = subject.tryEmit(state1)
        expect {
            that(success).isTrue()
            that(subject.value).isEqualTo(state1)
        }
    }

    test("flow emits initial and state1 when updating it to state1 when collecting") {
        runTest {
            val collected = mutableListOf<String>()
            val collectJob = launch {
                subject
                    .take(3)
                    .toCollection(collected)
            }
            runCurrent()
            subject.emit(state1)
            runCurrent()
            collectJob.cancel()
            expectThat(collected).containsExactly(initialState, state1)
        }
    }

    test("subscriberCount is initially 0") {
        expectThat(subject.subscriptionCount.value).describedAs("current subscriber count")
            .isEqualTo(0)
    }

    test("subscriberCount updates with subscribers") {
        runTest {
            val collectJob1 = launch {
                subject.collect {}
            }
            val collectJob2 = launch {
                subject.collect {}
            }
            runCurrent()
            expectThat(subject.subscriptionCount.value).describedAs("current subscriber count")
                .isEqualTo(2)
            collectJob1.cancel()
            collectJob2.cancel()
        }
    }

    test("subscriberCount does not update with subscribers to outgoing internal state") {
        runTest {
            val collectJob = launch {
                subject.outgoingState.collect {}
            }
            runCurrent()
            expectThat(subject.subscriptionCount.value).describedAs("current subscriber count")
                .isEqualTo(0)
            collectJob.cancel()
        }
    }

    test("storeSubscriberCount is initially 0") {
        expectThat(subject.storeSubscriberCount.value).describedAs("current subscriber count")
            .isEqualTo(0)
    }

    test("storeSubscriberCount updates with subscribers to outgoing state") {
        runTest {
            val collectJob1 = launch {
                subject.outgoingState.collect {}
            }
            val collectJob2 = launch {
                subject.outgoingState.collect {}
            }
            runCurrent()
            expectThat(subject.storeSubscriberCount.value).describedAs("current subscriber count")
                .isEqualTo(2)
            collectJob1.cancel()
            collectJob2.cancel()
        }
    }

    test("storeSubscriberCount does not update with subscribers to internal state") {
        runTest {
            val job = launch {
                subject.collect {}
            }
            expectThat(subject.storeSubscriberCount.value).describedAs("current subscriber count")
                .isEqualTo(0)
            job.cancel()
        }
    }

    test("slow collector of outgoingState does not miss any emissions") {
        runTest {
            val collected = mutableListOf<String>()
            val runJob = launch { subject.run() }
            val collectJob = launch {
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
            runJob.cancel()
            collectJob.cancel()
        }
    }

    test("throws exception containing state when update fails") {
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

    test("throws exception containing state when updateAndGet fails") {
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

    test("throws exception containing state when getAndUpdate fails") {
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
    test("transformers run when run") {
        val addIndexTransformer: Transformer<String> = { flow ->
            flow.withIndex()
                .map { "${it.value}-${it.index}" }
        }
        val dropOddTransformer: Transformer<String> = { flow ->
            flow.filter {
                val index = it.substringAfter("-").toInt()
                index % 2 == 0
            }
        }
        val subjectWithTransformers = StateManager(initialState, listOf(addIndexTransformer, dropOddTransformer))

        runTest {
            val result = mutableListOf<String>()
            val runJob1 = launch { subjectWithTransformers.run() }
            val runJob2 = launch { subjectWithTransformers.outgoingState.toCollection(result) }
            runCurrent()

            subjectWithTransformers.tryEmit("first")
            runCurrent()
            subjectWithTransformers.tryEmit("second")
            runCurrent()

            runJob1.cancel()
            runJob2.cancel()
            expectThat(result).containsExactly("initial-0", "second-2")
        }
    }

    test("subject with delaying transformer suspends emissions due to backpressure") {
        runTest {
            val runJob = launch { delayedSubject.run() }
            runCurrent()
            expectThrows<TimeoutCancellationException> {
                withTimeout(15) {
                    delayedSubject.emit("first")
                    runCurrent()
                    delayedSubject.emit("second")
                    runCurrent()
                }
            }
            runJob.cancel()
        }
    }

    test("subject with delaying transformer does not miss any emissions") {
        runTest {
            val values = mutableListOf<String>()
            val runJob = launch { delayedSubject.run() }
            val emitJob1 = launch {
                delayedSubject.emit("first")
            }
            val emitJob2 = launch {
                delayedSubject.emit("second")
            }
            runCurrent()
            values += delayedSubject.value
            advanceTimeBy(100)
            values += delayedSubject.value
            expectThat(values).containsExactly("first", "second")
            runJob.cancel()
            emitJob1.cancel()
            emitJob2.cancel()
        }
    }

    test("subject with delaying transformer, tryEmit fails to set new state and returns false when blocked") {
        runTest {
            val runJob = launch { delayedSubject.run() }
            runCurrent()
            delayedSubject.tryEmit(state1)
            val success = delayedSubject.tryEmit(state2)
            expect {
                that(success).isFalse()
                that(delayedSubject.value).isEqualTo(state1)
            }
            runJob.cancel()
        }
    }
})
