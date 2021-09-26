package se.gustavkarlsson.conveyor.plugin.vcr

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Plugin
import se.gustavkarlsson.conveyor.plugin.vcr.internal.Mode
import se.gustavkarlsson.conveyor.plugin.vcr.internal.PlaybackAction
import se.gustavkarlsson.conveyor.plugin.vcr.internal.RecordAction
import kotlin.time.TimeSource

public class VcrPlugin<State> : Vcr<State>, Plugin<State> {
    private val mode = MutableStateFlow<Mode<State>>(Mode.Idle) // FIXME Rename mode?

    override fun addStartActions(): Iterable<Action<State>> {
        val recordAction = RecordAction(mode, TimeSource.Monotonic)
        val playbackAction = PlaybackAction(mode)
        return listOf(recordAction, playbackAction)
    }

    // FIXME how to interrupt already running actions?
    override fun transformActions(actions: Flow<Action<State>>): Flow<Action<State>> =
        actions.filter { action ->
            when (mode.value) {
                Mode.Idle, is Mode.Recording -> true
                is Mode.Playing -> when (action) {
                    is PlaybackAction, is RecordAction -> true
                    else -> false
                }
            }
        }

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
