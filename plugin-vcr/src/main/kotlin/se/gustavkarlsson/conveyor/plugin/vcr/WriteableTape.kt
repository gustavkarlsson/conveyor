package se.gustavkarlsson.conveyor.plugin.vcr

import java.io.Closeable

public interface WriteableTape<in T> {
    public fun openForWriting(): Writing<T>

    public interface Writing<in T> : Closeable {
        public suspend fun write(sample: Sample<T>)
    }
}
