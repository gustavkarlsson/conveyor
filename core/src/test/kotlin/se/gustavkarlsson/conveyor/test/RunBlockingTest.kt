package se.gustavkarlsson.conveyor.test

import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.test.runBlockingTest as kotlinRunBlockingTest

/**
 * Same as [kotlinx.coroutines.test.runBlockingTest] but returns a value
 */
fun <T : Any> runBlockingTest(
    context: CoroutineContext = EmptyCoroutineContext,
    testBody: suspend TestCoroutineScope.() -> T,
): T {
    lateinit var result: T
    kotlinRunBlockingTest(context) {
        result = testBody()
    }
    return result
}

/**
 * Same as [kotlinx.coroutines.test.runBlockingTest] but returns a value
 */
fun <T : Any> TestCoroutineScope.runBlockingTest(
    block: suspend TestCoroutineScope.() -> T,
): T = runBlockingTest(coroutineContext, block)

/**
 * Same as [kotlinx.coroutines.test.runBlockingTest] but returns a value
 */
fun <T : Any> TestCoroutineDispatcher.runBlockingTest(
    block: suspend TestCoroutineScope.() -> T,
): T = runBlockingTest(this, block)
