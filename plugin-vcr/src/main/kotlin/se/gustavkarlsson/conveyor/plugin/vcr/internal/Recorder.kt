package se.gustavkarlsson.conveyor.plugin.vcr.internal

import se.gustavkarlsson.conveyor.Mapper
import se.gustavkarlsson.conveyor.plugin.vcr.Sample

// FIXME still doesn't handle first state
internal class Recorder<State>(
    private val getMode: () -> Mode<State>
) : Mapper<State> {
    override suspend fun map(value: State): State? {
        val mode = getMode()
        if (mode is Mode.Recording<State>) {
            val tape = mode.tape
            val delta = mode.trackPosition.getDelta()
            tape.write(Sample.Delay(delta))
            tape.write(Sample.State(value))
        }
        return value
    }
}
