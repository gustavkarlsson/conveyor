package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.flow.Flow

internal interface ReadableTape<State> {
    fun read(): Flow<State>
}
