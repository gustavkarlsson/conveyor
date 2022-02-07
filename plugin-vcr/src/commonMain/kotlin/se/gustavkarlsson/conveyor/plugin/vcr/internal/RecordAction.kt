package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreFlow
import se.gustavkarlsson.conveyor.plugin.vcr.Sample
import se.gustavkarlsson.conveyor.plugin.vcr.WriteableTape
import se.gustavkarlsson.conveyor.plugin.vcr.use
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal class RecordAction<State>(
    private val tape: WriteableTape<State>,
    private val bufferSize: Int,
    private val timeSource: TimeSource,
) : Action<State> {
    override suspend fun execute(storeFlow: StoreFlow<State>) {
        val writing = tape.openForWriting()
        val startTime = timeSource.markNow()
        writing.record(storeFlow, startTime, bufferSize)
    }
}

private suspend fun <State> WriteableTape.Writing<State>.record(
    storeFlow: Flow<State>,
    startTime: TimeMark,
    bufferSize: Int,
) = use { writing ->
    storeFlow
        .toSamples(startTime)
        .buffer(bufferSize)
        .collect(writing::write)
}

private fun <State> Flow<State>.toSamples(
    startTime: TimeMark,
) = flow {
    collect { state ->
        val timestampMillis = startTime.elapsedNow().inWholeMilliseconds
        val sample = Sample(timestampMillis, state)
        emit(sample)
    }
}
