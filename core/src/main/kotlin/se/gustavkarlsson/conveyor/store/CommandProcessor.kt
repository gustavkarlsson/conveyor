package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.CoroutineScope
import se.gustavkarlsson.conveyor.Command

internal interface CommandProcessor<State> {
    suspend fun process(scope: CoroutineScope)
    fun close(cause: Throwable?)
}
