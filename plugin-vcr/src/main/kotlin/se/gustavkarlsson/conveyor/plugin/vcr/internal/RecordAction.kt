package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.AtomicStateFlow
import se.gustavkarlsson.conveyor.plugin.vcr.Sample

internal class RecordAction<State>(
    private val mode: Flow<Mode<State>>,
    private val currentTimeMillis: () -> Long,
) : Action<State> {
    override suspend fun execute(stateFlow: AtomicStateFlow<State>) {
        mode.collectLatest { mode ->
            if (mode is Mode.Recording) {
                mode.record(stateFlow, currentTimeMillis)
            }
        }
    }
}

private suspend fun <State> Mode.Recording<State>.record(
    stateFlow: AtomicStateFlow<State>,
    currentTimeMillis: () -> Long,
) = writing.use { writing ->
    stateFlow.toSamples(currentTimeMillis)
        .buffer(bufferSize)
        .collect(writing::write)
}

private fun <State> AtomicStateFlow<State>.toSamples(
    currentTimeMillis: () -> Long,
) = flow {
    val timer = Timer(currentTimeMillis)
    collect { state ->
        val deltaMillis = timer.delta()
        if (deltaMillis != null) {
            emit(Sample.Delay(deltaMillis))
        }
        emit(Sample.State(state))
    }
}

private class Timer(private val getCurrent: () -> Long) {
    @Volatile
    private var previous: Long? = null

    @Synchronized
    fun delta(): Long? {
        val previous = previous
        val current = getCurrent()
        this.previous = current
        return if (previous == null) {
            null
        } else {
            current - previous
        }
    }
}
