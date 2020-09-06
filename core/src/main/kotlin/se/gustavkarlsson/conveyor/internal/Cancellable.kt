package se.gustavkarlsson.conveyor.internal

internal interface Cancellable {
    fun cancel(cause: Throwable? = null)
}
