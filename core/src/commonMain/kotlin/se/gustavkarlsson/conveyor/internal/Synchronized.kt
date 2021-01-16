package se.gustavkarlsson.conveyor.internal

internal expect inline fun <R>  synchronized(lock: Any, block: () -> R)
