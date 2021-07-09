package se.gustavkarlsson.conveyor

public interface DeadlockHandler {
    public sealed interface Resolution {
        public data class Retry(val timeoutMillis: Long) : Resolution
        public data class Throw(val exception: Exception) : Resolution
    }

    public val initialTimeoutMillis: Long
    public fun onDeadlock(count: Int): Resolution
}
