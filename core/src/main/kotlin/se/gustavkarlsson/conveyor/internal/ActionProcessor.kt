package se.gustavkarlsson.conveyor.internal

import se.gustavkarlsson.conveyor.UpdatableStateFlow

internal interface ActionProcessor<State> {
    suspend fun process(stateAccess: UpdatableStateFlow<State>)
}
