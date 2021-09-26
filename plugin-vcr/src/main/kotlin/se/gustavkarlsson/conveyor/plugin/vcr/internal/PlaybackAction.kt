package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.AtomicStateFlow
import se.gustavkarlsson.conveyor.plugin.vcr.ReadableTape
import se.gustavkarlsson.conveyor.plugin.vcr.Sample

internal class PlaybackAction<State>(
    private val mode: Flow<Mode<State>>,
) : Action<State> {
    override suspend fun execute(stateFlow: AtomicStateFlow<State>) {
        mode.collectLatest { mode ->
            if (mode is Mode.Playing) {
                mode.play(stateFlow)
            }
        }
    }
}

private suspend fun <State> Mode.Playing<State>.play(
    stateFlow: AtomicStateFlow<State>,
) = reading.use { reading ->
    reading.asFlow()
        .buffer(bufferSize)
        .collect { sample ->
            when (sample) {
                is Sample.Delay -> delay(sample.delayMillis)
                is Sample.State -> stateFlow.update { sample.state }
            }
        }
}

private fun <State> ReadableTape.Reading<State>.asFlow() = flow {
    while (true) {
        val sample = read()
        if (sample != null) {
            emit(sample)
        } else break
    }
}