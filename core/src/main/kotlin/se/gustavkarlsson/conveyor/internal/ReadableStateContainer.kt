package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.Flow

internal interface ReadableStateContainer<State> {
    val state: Flow<State>
    val currentState: State
}
