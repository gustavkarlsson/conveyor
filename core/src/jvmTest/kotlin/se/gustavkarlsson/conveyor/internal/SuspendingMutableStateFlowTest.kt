package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.testing.memoizedTestCoroutineScope
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull

object SuspendingMutableStateFlowTest : Spek({
    val scope by memoizedTestCoroutineScope()

    describe("A SuspendingMutableStateFlow") {
        val subject by memoized { SuspendingMutableStateFlow(0) }

        it("sets value when emitting") {
            runBlockingTest {
                subject.emit(5)
            }
            expectThat(subject.value).isEqualTo(5)
        }
        it("makes first value streamable") {
            var result: Any? = null
            runBlockingTest {
                result = subject.firstOrNull()
            }
            expectThat(result).isNotNull()
        }
        it("does not skip any items for slow collectors") {
            val targetCount = 10
            var count = 0
            scope.runBlockingTest {
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
            }
            expectThat(count).isEqualTo(targetCount)
        }
        it("rejects tryEmit when blocked") {
            var count = 0
            var success: Boolean? = null
            scope.runBlockingTest {
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
            }
            expect {
                that(success).isFalse()
                that(count).isEqualTo(1)
            }
        }
        it("initially has a subscriptionCount of 0") {
            expectThat(subject.subscriptionCount.value).isEqualTo(0)
        }
        it("has a subscriptionCount of 1 when one subscriber") {
            runBlockingTest {
                val job = launch { subject.collect() }
                expectThat(subject.subscriptionCount.value).isEqualTo(1)
                job.cancel()
            }
        }
    }
})
