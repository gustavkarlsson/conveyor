package se.gustavkarlsson.conveyor.internal

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
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
        /*
        runTest {
            FIXME Fix this test
            val targetCount = 10
            var count = 0
            launch {
                subject
                    .take(targetCount)
                    .collect {
                        delay(100)
                        count++
                    }
            }
            repeat(targetCount) {
                subject.emit(it + 1)
            }
            cancel()
            count.shouldBe(targetCount)
        }
         */
    }

    test("rejects tryEmit when blocked") {
        /*
        runTest {
            FIXME can't test this properly
            var count = 0
            var success: Boolean? = null
            launch {
                subject
                    .take(1)
                    .collect {
                        delay(100)
                        count++
                    }
            }
            subject.emit(2)
            success = subject.tryEmit(3)
            expect {
                that(success).isFalse()
                that(count).isEqualTo(1)
            }
        }
         */
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
