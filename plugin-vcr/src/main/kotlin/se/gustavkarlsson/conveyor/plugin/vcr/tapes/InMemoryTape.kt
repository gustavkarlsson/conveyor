package se.gustavkarlsson.conveyor.plugin.vcr.tapes

import se.gustavkarlsson.conveyor.plugin.vcr.ReadableTape
import se.gustavkarlsson.conveyor.plugin.vcr.Sample
import se.gustavkarlsson.conveyor.plugin.vcr.Tape
import se.gustavkarlsson.conveyor.plugin.vcr.WriteableTape

public class InMemoryTape<T> : Tape<T> {
    private val data = mutableListOf<Sample<T>>()

    override fun openForReading(): ReadableTape.Reading<T> = Reading()

    override fun openForWriting(): WriteableTape.Writing<T> = Writing()

    private inner class Reading : ReadableTape.Reading<T> {
        private val iterator = data.iterator()

        override suspend fun read(): Sample<T>? =
            if (iterator.hasNext()) {
                iterator.next()
            } else null

        override fun close() = Unit
    }

    private inner class Writing : WriteableTape.Writing<T> {
        override suspend fun write(sample: Sample<T>) {
            data += sample
        }

        override fun close() = Unit
    }
}
