package se.gustavkarlsson.conveyor.internal

import se.gustavkarlsson.conveyor.StateAccess

internal interface ActionProcessor<State> {
    suspend fun process(stateAccess: StateAccess<State>)
}
