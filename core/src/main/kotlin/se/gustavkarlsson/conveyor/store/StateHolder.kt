package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.flow.Flow

internal interface StateHolder<State> {
    val flow: Flow<State>
    var state: State
}
