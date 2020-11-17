package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.plugin.vcr.ReadableTape
import se.gustavkarlsson.conveyor.plugin.vcr.Sample

@ExperimentalCoroutinesApi
internal class PlaybackAction<State>(
    private val mode: Flow<Mode<State>>,
) : Action<State> {
    override suspend fun execute(state: UpdatableStateFlow<State>) {
        mode.collectLatest { mode ->
            if (mode is Mode.Playing) {
                mode.reading.playbackStates(state)
            }
        }
    }
}

private suspend fun <State> ReadableTape.Reading<State>.playbackStates(
    state: UpdatableStateFlow<State>,
) = use { reading ->
    while (true) {
        when (val sample = reading.read()) {
            is Sample.Delay -> delay(sample.timeMillis)
            is Sample.State -> state.update { sample.state }
            null -> break
        }
    }
}
