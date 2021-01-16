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
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo

object StateManagerTest : Spek({
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

        it("transformers run when launched") {
            val result = mutableListOf<String>()
            runBlockingTest {
                val launchJob = subject.launch(this)
                val collectJob = launch { subject.outgoingState.toCollection(result) }

                subject.update { "first" }
                subject.update { "second" }

                launchJob.cancel()
                collectJob.cancel()
            }
            expectThat(result).containsExactly("initial-0", "second-2")
        }
    }
    describe("A StateManager with a delaying transformer") {
        val scope by memoizedTestCoroutineScope()
        val delay: Transformer<String> = { flow ->
            flow.onEach { delay(10) }
        }
        val subject by memoized { StateManager(initialState, listOf(delay)) }
        beforeEachTest { subject.launch(scope) }

        it("suspends emissions due to backpressure") {
            expectThrows<TimeoutCancellationException> {
                withTimeout(15) {
                    subject.update { "first" }
                    subject.update { "second" }
                }
            }
        }
        it("does not miss any emissions") {
            val values = mutableListOf<String>()
            scope.runBlockingTest {
                launch {
                    subject.update { "first" }
                }
                launch {
                    subject.update { "second" }
                }
                values += subject.value
                advanceTimeBy(10)
                values += subject.value
            }
            expectThat(values).containsExactly("first", "second")
        }
    }
})
