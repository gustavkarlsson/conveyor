package se.gustavkarlsson.conveyor.internal

internal actual inline fun <R> synchronized(lock: Any, block: () -> R) {
    kotlin.synchronized(lock, block)
}
