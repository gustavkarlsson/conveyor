package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import se.gustavkarlsson.conveyor.Transformer
import se.gustavkarlsson.conveyor.plugin.vcr.Sample

internal class Recorder<State>(
    private val mode: Flow<Mode<State>>
) : Transformer<State> {
    override suspend fun transform(flow: Flow<State>): Flow<State> =
        combine(flow, mode) { state, mode ->
            if (mode is Mode.Recording<State>) {
                val tape = mode.tape
                val delta = mode.trackPosition.getDelta()
                tape.write(Sample.Delay(delta))
                tape.write(Sample.State(state))
            }
            state
        }
}
