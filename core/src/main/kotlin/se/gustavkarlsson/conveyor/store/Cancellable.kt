package se.gustavkarlsson.conveyor.store

internal interface Cancellable {
    fun cancel(cause: Throwable? = null)
}
