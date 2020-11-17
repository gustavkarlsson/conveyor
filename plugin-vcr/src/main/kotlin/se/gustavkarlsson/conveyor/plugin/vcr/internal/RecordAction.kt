package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.plugin.vcr.Sample
import se.gustavkarlsson.conveyor.plugin.vcr.WriteableTape

internal class RecordAction<State>(
    private val mode: Flow<Mode<State>>,
) : Action<State> {
    override suspend fun execute(state: UpdatableStateFlow<State>) {
        mode.collectLatest { mode ->
            if (mode is Mode.Recording) {
                mode.writing.recordStates(state, mode.trackPosition)
            }
        }
    }
}

private suspend fun <State> WriteableTape.Writing<State>.recordStates(
    state: UpdatableStateFlow<State>,
    trackPosition: TrackPosition,
) = use { writing ->
    state.collect { state ->
        val delta = trackPosition.getDelta()
        writing.write(Sample.Delay(delta))
        writing.write(Sample.State(state))
    }
}
