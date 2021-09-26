package se.gustavkarlsson.conveyor.plugin.vcr.internal

import se.gustavkarlsson.conveyor.plugin.vcr.ReadableTape
import se.gustavkarlsson.conveyor.plugin.vcr.WriteableTape

internal sealed interface Mode<out State> {
    object Idle : Mode<Nothing>

    data class Recording<State>(
        val writing: WriteableTape.Writing<State>,
        val bufferSize: Int,
    ) : Mode<State>

    data class Playing<State>(
        val reading: ReadableTape.Reading<State>,
        val bufferSize: Int,
    ) : Mode<State>
}
