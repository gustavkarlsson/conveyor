package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

internal interface Processor {
    fun process(scope: CoroutineScope): Job
}
