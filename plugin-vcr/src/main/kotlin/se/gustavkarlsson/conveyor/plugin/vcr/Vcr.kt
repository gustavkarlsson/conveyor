package se.gustavkarlsson.conveyor.plugin.vcr

import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Mapper
import se.gustavkarlsson.conveyor.Plugin
import se.gustavkarlsson.conveyor.plugin.vcr.internal.*

public class Vcr<State> : Plugin<State> {

    private val modeChannel = ConflatedBroadcastChannel<Mode<State>>(Mode.Idle)

    private val mode = modeChannel.asFlow()

    override fun overrideStateMappers(
        stateMappers: Iterable<Mapper<State>>
    ): Iterable<Mapper<State>> = stateMappers + Recorder(modeChannel::value)

    override fun overrideOpenActions(
        openActions: Iterable<Action<State>>
    ): Iterable<Action<State>> = openActions + PlaybackAction(mode)

    override fun overrideActionMappers(
        actionMappers: Iterable<Mapper<Action<State>>>
    ): Iterable<Mapper<Action<State>>> = actionMappers + PlaybackActionFilter(modeChannel::value)
}

internal sealed class Mode<out State> {
    object Idle : Mode<Nothing>()
    data class Recording<State>(val tape: WriteableTape<State>) : Mode<State>()
    data class Playing<State>(val tape: ReadableTape<State>) : Mode<State>()
}
