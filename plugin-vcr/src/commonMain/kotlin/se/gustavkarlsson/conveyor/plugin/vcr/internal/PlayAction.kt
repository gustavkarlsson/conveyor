package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreFlow
import se.gustavkarlsson.conveyor.plugin.vcr.ReadableTape
import se.gustavkarlsson.conveyor.plugin.vcr.use
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal class PlayAction<State>(
    private val tape: ReadableTape<State>,
    private val bufferSize: Int,
    private val timeSource: TimeSource,
) : Action<State> {
    override suspend fun execute(storeFlow: StoreFlow<State>) {
        val reading = tape.openForReading()
        val startTime = timeSource.markNow()
        reading.play(storeFlow, startTime, bufferSize)
    }
}

private suspend fun <State> ReadableTape.Reading<State>.play(
    storeFlow: MutableSharedFlow<State>,
    startTime: TimeMark,
    bufferSize: Int,
) = use { reading ->
    reading.asFlow()
        .buffer(bufferSize)
        .collect { sample ->
            val nowMillis = startTime.elapsedNow().inWholeMilliseconds
            val targetMillis = sample.timestampMillis
            val delayMillis = (targetMillis - nowMillis).coerceAtLeast(0)
            delay(delayMillis)
            storeFlow.emit(sample.state)
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
