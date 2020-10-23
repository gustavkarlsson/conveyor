package se.gustavkarlsson.conveyor.plugin.vcr

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Plugin
import se.gustavkarlsson.conveyor.Transformer
import se.gustavkarlsson.conveyor.plugin.vcr.internal.Mode
import se.gustavkarlsson.conveyor.plugin.vcr.internal.PlaybackAction
import se.gustavkarlsson.conveyor.plugin.vcr.internal.PlaybackActionFilter
import se.gustavkarlsson.conveyor.plugin.vcr.internal.RecordAction
import se.gustavkarlsson.conveyor.plugin.vcr.internal.TrackPosition

@FlowPreview
@ExperimentalCoroutinesApi
public class Vcr<State> : Plugin<State>, Control<State> {

    private val modeChannel = ConflatedBroadcastChannel<Mode<State>>(Mode.Idle)

    private val mode = modeChannel.asFlow()

    override fun overrideStartActions(
        startActions: Iterable<Action<State>>,
    ): Iterable<Action<State>> = startActions + RecordAction(mode) + PlaybackAction(mode)

    override fun overrideActionTransformers(
        actionTransformers: Iterable<Transformer<Action<State>>>,
    ): Iterable<Transformer<Action<State>>> = actionTransformers + PlaybackActionFilter(mode)

    override fun play(tape: ReadableTape<State>) {
        modeChannel.offer(Mode.Playing(tape))
    }

    override fun record(tape: WriteableTape<State>) {
        val trackPosition = TrackPosition(System::currentTimeMillis)
        trackPosition.start()
        modeChannel.offer(Mode.Recording(tape, trackPosition))
    }

    override fun stop() {
        modeChannel.offer(Mode.Idle)
    }
}
