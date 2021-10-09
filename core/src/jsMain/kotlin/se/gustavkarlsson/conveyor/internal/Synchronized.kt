package se.gustavkarlsson.conveyor.internal

@Suppress("UnusedPrivateMember")
internal actual inline  fun <R> synchronized(lock: Any, block: () -> R) {
    block()
}
