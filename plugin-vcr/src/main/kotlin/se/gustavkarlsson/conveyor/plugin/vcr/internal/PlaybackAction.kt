package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.plugin.vcr.Sample

internal class PlaybackAction<State>(
    private val mode: Flow<Mode<State>>,
) : Action<State> {
    override suspend fun execute(state: UpdatableStateFlow<State>) {
        mode.collectLatest { mode ->
            if (mode is Mode.Playing) {
                mode.play(state)
            }
        }
    }
}

private suspend fun <State> Mode.Playing<State>.play(
    state: UpdatableStateFlow<State>,
) = reading.use { reading ->
    while (true) {
        when (val sample = reading.read()) {
            is Sample.Delay -> delay(sample.delayMillis)
            is Sample.State -> state.update { sample.state }
            null -> break
        }
    }
}
