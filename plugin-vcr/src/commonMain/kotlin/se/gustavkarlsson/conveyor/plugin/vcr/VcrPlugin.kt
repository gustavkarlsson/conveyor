package se.gustavkarlsson.conveyor.plugin.vcr

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Plugin
import se.gustavkarlsson.conveyor.plugin.vcr.internal.PlayAction
import se.gustavkarlsson.conveyor.plugin.vcr.internal.RecordAction
import kotlin.time.TimeSource

public class VcrPlugin<State>(private val mode: Mode<State>) : Plugin<State> {

    override fun addStartActions(): Iterable<Action<State>> {
        val timeSource = TimeSource.Monotonic
        val action = when (mode) {
            is Mode.Record -> RecordAction(mode.tape, mode.bufferSize, timeSource)
            is Mode.Play -> PlayAction(mode.tape, mode.bufferSize, timeSource)
        }
        return listOf(action)
    }

    override fun transformActions(actions: Flow<Action<State>>): Flow<Action<State>> =
        actions.filter { action ->
            if (action is PlayAction || action is RecordAction) return@filter true
            when (mode) {
                is Mode.Record -> true
                is Mode.Play -> false
            }
        }
}
