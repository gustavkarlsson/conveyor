package se.gustavkarlsson.conveyor.plugin.vcr.tapes

import se.gustavkarlsson.conveyor.plugin.vcr.ReadableTape
import se.gustavkarlsson.conveyor.plugin.vcr.Sample
import se.gustavkarlsson.conveyor.plugin.vcr.Tape
import se.gustavkarlsson.conveyor.plugin.vcr.WriteableTape

public class InMemoryTape<State> : Tape<State> {
    private val data = mutableListOf<Sample<State>>()

    override fun openForReading(): ReadableTape.Reading<State> = Reading()

    override fun openForWriting(): WriteableTape.Writing<State> = Writing()

    private inner class Reading : ReadableTape.Reading<State> {
        private val iterator = data.iterator()

        override suspend fun read(): Sample<State>? =
            if (iterator.hasNext()) {
                iterator.next()
            } else null

        override fun close() = Unit
    }

    private inner class Writing : WriteableTape.Writing<State> {
        override suspend fun write(sample: Sample<State>) {
            data += sample
        }

        override fun close() = Unit
    }
}
