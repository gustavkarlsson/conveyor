package se.gustavkarlsson.conveyor.testing

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineExceptionHandler
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.createTestCoroutineScope
import org.spekframework.spek2.dsl.LifecycleAware
import org.spekframework.spek2.lifecycle.MemoizedValue

fun LifecycleAware.memoizedTestCoroutineScope(): MemoizedValue<TestCoroutineScope> =
    memoized(
        factory = { createTestCoroutineScope(TestCoroutineDispatcher() + TestCoroutineExceptionHandler() + Job()) },
        destructor = {
            it.cancel("Test ended")
            it.cleanupTestCoroutines()
        }
    )
