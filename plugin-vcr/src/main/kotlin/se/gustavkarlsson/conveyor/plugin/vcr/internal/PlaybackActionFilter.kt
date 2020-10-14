package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Transformer

internal class PlaybackActionFilter<State>(
    private val mode: Flow<Mode<State>>
) : Transformer<Action<State>> {
    override suspend fun transform(flow: Flow<Action<State>>): Flow<Action<State>> =
        combineTransform(flow, mode) { state, mode ->
            if (mode !is Mode.Playing<*>) {
                emit(state)
            }
        }
}
