package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.CoroutineScope

internal interface LiveActionsProcessor {
    suspend fun increaseLiveCount()
    suspend fun decreaseLiveCount()
    suspend fun process(scope: CoroutineScope)
    fun close(cause: Throwable?)
}
