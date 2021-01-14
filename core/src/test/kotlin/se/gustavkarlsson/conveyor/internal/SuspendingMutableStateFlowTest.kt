package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.testing.memoizedTestCoroutineScope
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import strikt.assertions.isLessThan

object SuspendingMutableStateFlowTest : Spek({
    val scope by memoizedTestCoroutineScope()

    describe("A SuspendingMutableStateFlow that suspends") {
        val subject by memoized { SuspendingMutableStateFlow(0, suspend = true) }

        it("sets value when emitting") {
            runBlockingTest {
                subject.emit(5)
            }
            expectThat(subject.value).isEqualTo(5)
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
    }
    describe("A SuspendingMutableStateFlow that does not suspend") {
        val subject by memoized { SuspendingMutableStateFlow(0, suspend = false) }

        it("always has the latest value even with slow collectors") {
            var collectedValues = 0
            runBlockingTest {
                val job = launch {
                    subject
                        .onEach { delay(20) }
                        .collect { collectedValues++ }
                }
                repeat(100) {
                    subject.emit(it + 1)
                    delay(10)
                }
                job.cancel()
                expectThat(collectedValues)
                    .isGreaterThan(10)
                    .isLessThan(90)
            }
        }
    }
})
