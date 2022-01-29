package se.gustavkarlsson.conveyor.testing

import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import org.spekframework.spek2.dsl.LifecycleAware
import org.spekframework.spek2.lifecycle.MemoizedValue

// FIXME replace with runTest ?
fun LifecycleAware.memoizedTestCoroutineScope(): MemoizedValue<TestScope> =
    memoized(
        factory = { TestScope() },
        destructor = {
            it.cancel("Test ended")
        }
    )
