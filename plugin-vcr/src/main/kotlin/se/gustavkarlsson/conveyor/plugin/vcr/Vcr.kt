package se.gustavkarlsson.conveyor.plugin.vcr

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val mode = MutableStateFlow<Mode<State>>(Mode.Idle)

    override fun overrideStartActions(
        startActions: Iterable<Action<State>>,
    ): Iterable<Action<State>> = startActions + RecordAction(mode) + PlaybackAction(mode)

    override fun overrideActionTransformers(
        actionTransformers: Iterable<Transformer<Action<State>>>,
    ): Iterable<Transformer<Action<State>>> = actionTransformers + PlaybackActionFilter(mode)

    override fun play(tape: ReadableTape<State>) {
        val reading = tape.openForReading()
        mode.value = Mode.Playing(reading)
    }

    override fun record(tape: WriteableTape<State>) {
        val writing = tape.openForWriting()
        val trackPosition = TrackPosition(System::currentTimeMillis)
        trackPosition.start()
        mode.value = Mode.Recording(writing, trackPosition)
    }

    override fun stop() {
        mode.value = Mode.Idle
    }
}
