package se.gustavkarlsson.conveyor.plugin.vcr.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess
import se.gustavkarlsson.conveyor.plugin.vcr.Sample

internal class RecordAction<State>(
    private val mode: Flow<Mode<State>>,
) : Action<State> {
    override suspend fun execute(stateAccess: StateAccess<State>) {
        combine(stateAccess.flow, mode) { state, mode ->
            if (mode is Mode.Recording<State>) {
                val tape = mode.tape
                val delta = mode.trackPosition.getDelta()
                tape.write(Sample.Delay(delta))
                tape.write(Sample.State(state))
            }
            Unit
        }.collect()
    }
}
