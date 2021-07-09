package se.gustavkarlsson.conveyor

public interface LockHandler {
    public sealed interface Resolution {
        public data class Retry(val timeoutMillis: Long) : Resolution
        public data class Throw(val exception: Exception) : Resolution
    }

    public val initialTimeoutMillis: Long
    public fun onLock(retries: Int): Resolution

    public object Ignore : LockHandler {
        override val initialTimeoutMillis: Long = Long.MAX_VALUE
        override fun onLock(retries: Int): Resolution = error("This should never happen")
    }

    public class Throw(timeoutMillis: Long) : LockHandler {
        override val initialTimeoutMillis: Long = timeoutMillis
        override fun onLock(retries: Int): Resolution {
            val message = "Lock detected after ${initialTimeoutMillis}ms. " +
                "Are you updating the state faster than it is collected?"
            return Resolution.Throw(Exception(message))
        }
    }
}
