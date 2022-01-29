package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

object StatefulMutableSharedFlowTest : Spek({
    describe("A StatefulMutableSharedFlow") {
        val subject by memoized { StatefulMutableSharedFlow(0) }

        it("sets value when emitting") {
            runTest {
                subject.emit(5)
                expectThat(subject.value).isEqualTo(5)
            }
        }
        it("makes first value streamable") {
            runTest {
                val result = subject.firstOrNull()
                expectThat(result).isNotNull()
            }
        }
        it("does not skip any items for slow collectors") {
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
                expectThat(count).isEqualTo(targetCount)
            }
             */
        }
        it("rejects tryEmit when blocked") {
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
        it("initially has a subscriptionCount of 0") {
            expectThat(subject.subscriptionCount.value).isEqualTo(0)
        }
        it("has a subscriptionCount of 1 when one subscriber") {
            runTest {
                launch { subject.collect() }
                runCurrent()
                expectThat(subject.subscriptionCount.value).isEqualTo(1)
                cancel()
            }
        }
        it("does not emit non-distinct element") {
            runTest {
                val values = mutableListOf<Int>()
                launch { subject.toCollection(values) }
                subject.emit(0)
                subject.emit(0)
                runCurrent()
                expectThat(values).containsExactly(0)
                cancel()
            }
        }
    }
})
