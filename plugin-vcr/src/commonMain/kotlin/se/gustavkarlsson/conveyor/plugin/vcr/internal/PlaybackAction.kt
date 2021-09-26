package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreFlow
import se.gustavkarlsson.conveyor.plugin.vcr.ReadableTape
import se.gustavkarlsson.conveyor.plugin.vcr.use
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal class PlaybackAction<State>(
    private val mode: Flow<Mode<State>>,
    private val timeSource: TimeSource,
) : Action<State> {
    override suspend fun execute(storeFlow: StoreFlow<State>) {
        mode.collectLatest { mode ->
            if (mode is Mode.Playing) {
                val playbackStart = timeSource.markNow()
                mode.play(storeFlow, playbackStart)
            }
        }
    }
}

private suspend fun <State> Mode.Playing<State>.play(
    storeFlow: StoreFlow<State>,
    playbackStart: TimeMark,
) = reading.use { reading ->
    reading.asFlow()
        .buffer(bufferSize)
        .collect { sample ->
            val nowMillis = playbackStart.elapsedNow().inWholeMilliseconds
            val targetMillis = sample.timestampMillis
            val delayMillis = (targetMillis - nowMillis).coerceAtLeast(0)
            delay(delayMillis)
            storeFlow.update { sample.state }
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
