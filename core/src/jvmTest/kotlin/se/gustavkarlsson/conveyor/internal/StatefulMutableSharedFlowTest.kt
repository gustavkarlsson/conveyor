package se.gustavkarlsson.conveyor.internal

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class StatefulMutableSharedFlowTest : FunSpec({
    val subject = StatefulMutableSharedFlow(0)

    test("sets value when emitting") {
        runTest {
            subject.emit(5)
            subject.value.shouldBe(5)
        }
    }

    test("makes first value streamable") {
        runTest {
            val result = subject.firstOrNull()
            result.shouldNotBeNull()
        }
    }

    test("does not skip any items for slow collectors") {
        runTest {
            val targetCount = 10
            val itemDelay = 100L
            var count = 0
            launch {
                subject
                    .buffer(targetCount)
                    .take(targetCount)
                    .collect {
                        count++
                        delay(itemDelay)
                    }
            }
            runCurrent()
            repeat(targetCount) {
                launch { subject.emit(it + 1) }
            }
            advanceTimeBy(itemDelay * targetCount)
            count.shouldBe(targetCount)
        }
    }

    test("rejects tryEmit when blocked") {
        runTest {
            var count = 0
            val collectJob = launch {
                subject
                    .collect {
                        count++
                        delay(100)
                    }
            }
            runCurrent()
            subject.emit(2)
            val success = subject.tryEmit(3)
            assertSoftly {
                success.shouldBeFalse()
                count.shouldBe(1)
            }
            collectJob.cancel()
        }
    }

    test("initially has a subscriptionCount of 0") {
        subject.subscriptionCount.value.shouldBe(0)
    }

    test("has a subscriptionCount of 1 when one subscriber") {
        runTest {
            val collectJob = launch { subject.collect() }
            runCurrent()
            subject.subscriptionCount.value.shouldBe(1)
            collectJob.cancel()
        }
    }

    test("does not emit non-distinct element") {
        runTest {
            val values = mutableListOf<Int>()
            val collectJob = launch { subject.toCollection(values) }
            subject.emit(0)
            subject.emit(0)
            runCurrent()
            values.shouldContainExactly(0)
            collectJob.cancel()
        }
    }
})
