package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

internal fun interface Launcher {
    fun launch(scope: CoroutineScope): Job
}
