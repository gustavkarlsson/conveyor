package se.gustavkarlsson.conveyor.plugin.vcr

import java.io.Closeable

public interface ReadableTape<T> {
    public fun openForReading(): Reading<T>

    public interface Reading<T> : Closeable {
        public suspend fun read(): Sample<T>?
    }
}
