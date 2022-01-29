package se.gustavkarlsson.conveyor.testing

import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest as kotlinRunTest
import kotlin.coroutines.CoroutineContext

/**
 * Same as [kotlinx.coroutines.test.runTest] but returns a value
 */
fun <T : Any> runTest(
    context: CoroutineContext = UnconfinedTestDispatcher(),
    testBody: suspend TestScope.() -> T,
): T {
    lateinit var result: T
    kotlinRunTest(context) {
        result = testBody()
    }
    return result
}

/**
 * Same as [kotlinx.coroutines.test.runTest] but returns a value
 */
fun <T : Any> TestScope.runTest(
    block: suspend TestScope.() -> T,
): T = runTest(coroutineContext, block)

/**
 * Same as [kotlinx.coroutines.test.runTest] but returns a value
 */
fun <T : Any> TestDispatcher.runTest(
    block: suspend TestScope.() -> T,
): T = runTest(this, block)
