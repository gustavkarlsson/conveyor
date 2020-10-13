package se.gustavkarlsson.conveyor.plugin.vcr.internal

import se.gustavkarlsson.conveyor.Mapper
import se.gustavkarlsson.conveyor.plugin.vcr.Mode

internal class Recorder<State>(
    private val getMode: () -> Mode<State>
) : Mapper<State> {
    override fun map(value: State): State? {
        val mode = getMode()
        if (mode is Mode.Recording<State>) {
            // FIXME re-enable after suspending map: mode.tape.write(value)
        }
        return value
    }

    private fun record(state: State) {
        TODO("Not yet implemented")
    }
}
