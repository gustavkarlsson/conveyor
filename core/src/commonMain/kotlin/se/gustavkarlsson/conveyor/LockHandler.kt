package se.gustavkarlsson.conveyor

public interface LockHandler {
    public sealed interface Resolution {
        public data class Retry(val timeoutMillis: Long) : Resolution
        public data class Throw(val exception: Exception) : Resolution
    }

    public val initialTimeoutMillis: Long
    public fun onLock(retries: Int): Resolution
}
