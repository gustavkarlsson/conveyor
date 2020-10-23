package se.gustavkarlsson.conveyor.test

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineScope
import org.spekframework.spek2.dsl.Root
import org.spekframework.spek2.lifecycle.MemoizedValue

fun Root.memoizedTestCoroutineScope(): MemoizedValue<TestCoroutineScope> =
    memoized(
        factory = { TestCoroutineScope(Job()) },
        destructor = {
            it.cancel("Test ended")
            it.cleanupTestCoroutines()
        }
    )
