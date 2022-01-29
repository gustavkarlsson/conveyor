package se.gustavkarlsson.conveyor.testing

import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.test.runTest as kotlinRunTest

const val DEFAULT_DISPATCH_TIMEOUT_MS = 5L

/**
 * Same as [kotlinx.coroutines.test.runTest] but returns a value
 */
fun <T : Any> runTest(
    context: CoroutineContext = EmptyCoroutineContext,
    dispatchTimeoutMs: Long = DEFAULT_DISPATCH_TIMEOUT_MS,
    testBody: suspend TestScope.() -> T,
): T {
    lateinit var result: T
    kotlinRunTest(context, dispatchTimeoutMs) {
        result = testBody()
    }
    return result
}

/**
 * Same as [kotlinx.coroutines.test.runTest] but returns a value
 */
fun <T : Any> TestScope.runTest(
    dispatchTimeoutMs: Long = DEFAULT_DISPATCH_TIMEOUT_MS,
    testBody: suspend TestScope.() -> T,
): T = runTest(coroutineContext, dispatchTimeoutMs, testBody)

/**
 * Same as [kotlinx.coroutines.test.runTest] but returns a value
 */
fun <T : Any> TestDispatcher.runTest(
    dispatchTimeoutMs: Long = DEFAULT_DISPATCH_TIMEOUT_MS,
    testBody: suspend TestScope.() -> T,
): T = runTest(this, dispatchTimeoutMs, testBody)
