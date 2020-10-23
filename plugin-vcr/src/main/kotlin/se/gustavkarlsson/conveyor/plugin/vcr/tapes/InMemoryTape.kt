package se.gustavkarlsson.conveyor.plugin.vcr.tapes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import se.gustavkarlsson.conveyor.plugin.vcr.ReadableTape
import se.gustavkarlsson.conveyor.plugin.vcr.Sample
import se.gustavkarlsson.conveyor.plugin.vcr.WriteableTape

public class InMemoryTape<T> : ReadableTape<T>, WriteableTape<T> {
    private val data = mutableListOf<Sample<T>>()

    override fun read(): Flow<Sample<T>> = data.asFlow()

    override suspend fun write(sample: Sample<T>) {
        data += sample
    }

    public fun clear() {
        data.clear()
    }
}
