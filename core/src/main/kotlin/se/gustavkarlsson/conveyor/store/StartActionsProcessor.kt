package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.CoroutineScope

internal interface StartActionsProcessor {
    suspend fun process(scope: CoroutineScope)
}
