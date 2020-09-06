package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.flow.Flow

internal interface StateContainer<State> {
    val state: Flow<State>
    var currentState: State
}
