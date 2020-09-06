package se.gustavkarlsson.conveyor.store

internal interface LiveActionsCounter {
    fun increment()
    fun decrement()
}
