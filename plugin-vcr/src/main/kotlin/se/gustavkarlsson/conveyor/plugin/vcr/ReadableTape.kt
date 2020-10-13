package se.gustavkarlsson.conveyor.plugin.vcr

import kotlinx.coroutines.flow.Flow

public interface ReadableTape<State> {
    public fun read(): Flow<State>
}
