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
import kotlin.jvm.Synchronized
import kotlin.jvm.Volatile
import kotlin.time.TimeMark
import kotlin.time.TimeSource

internal class RecordAction<State>(
    private val mode: Flow<Mode<State>>,
    private val timeSource: TimeSource,
) : Action<State> {
    override suspend fun execute(storeFlow: StoreFlow<State>) {
        mode.collectLatest { mode ->
            if (mode is Mode.Recording) {
                val timeMark = timeSource.markNow()
                mode.record(storeFlow, timeMark)
            }
        }
    }
}

private suspend fun <State> Mode.Recording<State>.record(
    storeFlow: StoreFlow<State>,
    timeMark: TimeMark,
) = writing.use { writing ->
    storeFlow.toSamples(timeMark)
        .buffer(bufferSize)
        .collect(writing::write)
}

private fun <State> StoreFlow<State>.toSamples(
    timeMark: TimeMark,
) = flow {
    val timer = Timer(timeMark)
    collect { state ->
        val deltaMillis = timer.delta()
        if (deltaMillis != null) {
            emit(Sample.Delay(deltaMillis))
        }
        emit(Sample.State(state))
    }
}

private class Timer(private val timeMark: TimeMark) {
    @Volatile
    private var previous: Long? = null

    @Synchronized
    fun delta(): Long? {
        val previous = previous
        val current = timeMark.elapsedNow().inWholeMilliseconds
        this.previous = current
        return if (previous == null) {
            null
        } else {
            current - previous
        }
    }
}
