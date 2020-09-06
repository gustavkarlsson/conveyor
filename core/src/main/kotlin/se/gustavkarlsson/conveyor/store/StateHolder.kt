package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.flow.Flow

internal interface StateHolder<State> {
    val flow: Flow<State>
    fun get(): State
    fun set(state: State)
    fun close(cause: Throwable?)
}
