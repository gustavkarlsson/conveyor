package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Transformer

@ExperimentalCoroutinesApi
internal class PlaybackActionFilter<State>(
    private val mode: Flow<Mode<State>>,
) : Transformer<Action<State>> {
    override fun transform(flow: Flow<Action<State>>): Flow<Action<State>> =
        combineTransform(flow, mode) { action, mode ->
            when (mode) {
                Mode.Idle, is Mode.Recording -> emit(action)
                is Mode.Playing -> when (action) {
                    is PlaybackAction, is RecordAction -> emit(action)
                }
            }
        }
}
