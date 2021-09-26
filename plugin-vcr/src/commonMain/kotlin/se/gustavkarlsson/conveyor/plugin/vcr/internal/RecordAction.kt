package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreFlow
import se.gustavkarlsson.conveyor.plugin.vcr.Sample
import se.gustavkarlsson.conveyor.plugin.vcr.use
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal class RecordAction<State>(
    private val mode: Flow<Mode<State>>,
    private val timeSource: TimeSource,
) : Action<State> {
    override suspend fun execute(storeFlow: StoreFlow<State>) {
        mode.collectLatest { mode ->
            if (mode is Mode.Recording) {
                val recordingStart = timeSource.markNow()
                mode.record(storeFlow, recordingStart)
            }
        }
    }
}

private suspend fun <State> Mode.Recording<State>.record(
    storeFlow: Flow<State>,
    recordingStart: TimeMark,
) = writing.use { writing ->
    storeFlow
        .toSamples(recordingStart)
        .buffer(bufferSize)
        .collect(writing::write)
}

private fun <State> Flow<State>.toSamples(
    recordingStart: TimeMark,
) = flow {
    collect { state ->
        val timestampMillis = recordingStart.elapsedNow().inWholeMilliseconds
        val sample = Sample(timestampMillis, state)
        emit(sample)
    }
}
