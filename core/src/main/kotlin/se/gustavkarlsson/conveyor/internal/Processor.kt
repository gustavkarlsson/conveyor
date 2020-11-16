package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

internal fun interface Processor {
    fun process(scope: CoroutineScope): Job
}
