package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.Flow

internal interface StateFlowProvider<State> {
    val stateFlow: Flow<State>
}
