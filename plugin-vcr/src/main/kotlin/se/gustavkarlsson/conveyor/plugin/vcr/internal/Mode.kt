package se.gustavkarlsson.conveyor.plugin.vcr.internal

import se.gustavkarlsson.conveyor.plugin.vcr.ReadableTape
import se.gustavkarlsson.conveyor.plugin.vcr.WriteableTape

internal sealed class Mode<out State> {
    object Idle : Mode<Nothing>()

    data class Recording<State>(
        val writing: WriteableTape.Writing<State>,
        val trackPosition: TrackPosition,
    ) : Mode<State>()

    data class Playing<State>(
        val reading: ReadableTape.Reading<State>,
        val bufferSize: Int,
    ) : Mode<State>()
}
