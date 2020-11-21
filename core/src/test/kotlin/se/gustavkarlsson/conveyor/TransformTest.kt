package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.internal.transform
import se.gustavkarlsson.conveyor.testing.runBlockingTest
import strikt.api.expectThat
import strikt.assertions.containsExactly

object TransformTest : Spek({
    describe("A flow") {
        val flow = flowOf(1, 2, 3)

        it("returns same flow if transforming with 0 transformers") {
            val result = runBlockingTest {
                flow.transform(emptyList()).toList()
            }
            expectThat(result).containsExactly(1, 2, 3)
        }
        it("returns transformed flow after multiple transformations") {
            val doubleEach = Transformer<Int> {
                it.map { item -> item * 2 }
            }
            val filterOverThree = Transformer<Int> {
                it.filter { item -> item > 3 }
            }
            val result = runBlockingTest {
                flow.transform(listOf(doubleEach, filterOverThree)).toList()
            }
            expectThat(result).containsExactly(4, 6)
        }
    }
})
