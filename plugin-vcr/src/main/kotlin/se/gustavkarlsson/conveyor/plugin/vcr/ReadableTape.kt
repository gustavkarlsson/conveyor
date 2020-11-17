package se.gustavkarlsson.conveyor.plugin.vcr

import java.io.Closeable

public interface ReadableTape<out T> {
    public fun openForReading(): Reading<T>

    public interface Reading<out T> : Closeable {
        public suspend fun read(): Sample<T>?
    }
}
