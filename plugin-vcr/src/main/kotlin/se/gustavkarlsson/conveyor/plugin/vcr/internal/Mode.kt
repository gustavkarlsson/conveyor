package se.gustavkarlsson.conveyor.plugin.vcr.internal

import se.gustavkarlsson.conveyor.plugin.vcr.ReadableTape
import se.gustavkarlsson.conveyor.plugin.vcr.Sample
import se.gustavkarlsson.conveyor.plugin.vcr.TrackPosition
import se.gustavkarlsson.conveyor.plugin.vcr.WriteableTape

internal sealed class Mode<out State> {
    object Idle : Mode<Nothing>()

    data class Recording<State>(
        val tape: WriteableTape<Sample<State>>,
        val trackPosition: TrackPosition,
    ) : Mode<State>()

    data class Playing<State>(val tape: ReadableTape<State>) : Mode<State>()
}
