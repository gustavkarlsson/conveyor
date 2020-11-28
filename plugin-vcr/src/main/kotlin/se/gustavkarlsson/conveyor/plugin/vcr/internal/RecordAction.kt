package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.plugin.vcr.Sample

internal class RecordAction<State>(
    private val mode: Flow<Mode<State>>,
) : Action<State> {
    override suspend fun execute(state: UpdatableStateFlow<State>) {
        mode.collectLatest { mode ->
            if (mode is Mode.Recording) {
                mode.record(state)
            }
        }
    }
}

private suspend fun <State> Mode.Recording<State>.record(
    state: UpdatableStateFlow<State>,
) = writing.use { writing ->
    state.withDelays()
        .buffer(bufferSize)
        .collect { (state, delayMillis) ->
            if (delayMillis > 0) {
                writing.write(Sample.Delay(delayMillis))
            }
            writing.write(Sample.State(state))
        }
}

private fun <State> UpdatableStateFlow<State>.withDelays() = flow {
    val timer = Timer(System::currentTimeMillis)
    collect { state ->
        val delta = timer.delta()
        emit(state to delta)
    }
}

private class Timer(
    private val getCurrent: () -> Long,
) {
    private var last: Long? = null

    @Synchronized
    fun delta(): Long {
        val last = last
        return if (last == null) {
            this.last = getCurrent()
            0
        } else {
            val current = getCurrent()
            val delta = current - last
            this.last = current
            delta
        }
    }
}
