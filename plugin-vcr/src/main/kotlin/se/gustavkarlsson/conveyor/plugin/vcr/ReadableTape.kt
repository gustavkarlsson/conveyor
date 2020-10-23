package se.gustavkarlsson.conveyor.plugin.vcr

import kotlinx.coroutines.flow.Flow

public interface ReadableTape<T> {
    public fun read(): Flow<Sample<T>>
}
