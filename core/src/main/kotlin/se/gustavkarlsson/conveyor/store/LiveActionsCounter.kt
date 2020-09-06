package se.gustavkarlsson.conveyor.store

internal interface LiveActionsCounter {
    suspend fun increaseLiveCount()
    suspend fun decreaseLiveCount()
}
