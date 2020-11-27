package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.plugin.vcr.Sample

internal class RecordAction<State>(
    private val mode: Flow<Mode<State>>,
) : Action<State> {
    override suspend fun execute(state: UpdatableStateFlow<State>) {
        mode.collectLatest { mode ->
            if (mode is Mode.Recording) {
                mode.record(state)
            }
        }
    }
}

private suspend fun <State> Mode.Recording<State>.record(
    state: UpdatableStateFlow<State>,
) = writing.use { writing ->
    state.collect { state ->
        val delta = trackPosition.getDelta()
        writing.write(Sample.Delay(delta))
        writing.write(Sample.State(state))
    }
}
