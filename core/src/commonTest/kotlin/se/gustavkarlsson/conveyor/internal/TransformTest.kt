package se.gustavkarlsson.conveyor.internal

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest

class TransformTest : FunSpec({
    val flow = flowOf(1, 2, 3)

    test("returns same flow if transforming with 0 transformers") {
        runTest {
            val result = flow.transform(emptyList()).toList()
            result.shouldContainExactly(1, 2, 3)
        }
    }

    test("returns transformed flow after multiple transformations") {
        val doubleEach = { flow: Flow<Int> ->
            flow.map { item -> item * 2 }
        }
        val filterOverThree = { flow: Flow<Int> ->
            flow.filter { item -> item > 3 }
        }
        runTest {
            val result = flow.transform(listOf(doubleEach, filterOverThree)).toList()
            result.shouldContainExactly(4, 6)
        }
    }
})
