package se.gustavkarlsson.conveyor.testing

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineScope
import org.spekframework.spek2.dsl.GroupBody
import org.spekframework.spek2.lifecycle.MemoizedValue

fun GroupBody.memoizedTestCoroutineScope(): MemoizedValue<TestCoroutineScope> =
    memoized(
        factory = { TestCoroutineScope(Job()) },
        destructor = {
            it.cancel("Test ended")
            it.cleanupTestCoroutines()
        }
    )
