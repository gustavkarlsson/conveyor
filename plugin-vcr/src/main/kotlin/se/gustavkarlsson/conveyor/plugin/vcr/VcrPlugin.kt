package se.gustavkarlsson.conveyor.plugin.vcr

import kotlinx.coroutines.flow.MutableStateFlow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Plugin
import se.gustavkarlsson.conveyor.Transformer
import se.gustavkarlsson.conveyor.plugin.vcr.internal.Mode
import se.gustavkarlsson.conveyor.plugin.vcr.internal.PlaybackAction
import se.gustavkarlsson.conveyor.plugin.vcr.internal.PlaybackActionFilter
import se.gustavkarlsson.conveyor.plugin.vcr.internal.RecordAction

public class VcrPlugin<State> : Vcr<State>, Plugin<State> {
    private val mode = MutableStateFlow<Mode<State>>(Mode.Idle) // FIXME Rename mode?

    override fun overrideStartActions(
        startActions: Iterable<Action<State>>,
    ): Iterable<Action<State>> = startActions + RecordAction(mode) + PlaybackAction(mode)

    override fun overrideActionTransformers(
        actionTransformers: Iterable<Transformer<Action<State>>>,
    ): Iterable<Transformer<Action<State>>> = actionTransformers + PlaybackActionFilter(mode)

    override fun play(tape: ReadableTape<State>, bufferSize: Int) {
        val reading = tape.openForReading()
        mode.value = Mode.Playing(reading, bufferSize)
    }

    override fun record(tape: WriteableTape<State>, bufferSize: Int) {
        val writing = tape.openForWriting()
        mode.value = Mode.Recording(writing, bufferSize)
    }

    override fun stop() {
        mode.value = Mode.Idle
    }
}
