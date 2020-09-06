package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.CoroutineScope

internal interface Processor {
    suspend fun process(scope: CoroutineScope)
}
