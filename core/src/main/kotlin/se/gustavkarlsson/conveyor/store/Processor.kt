package se.gustavkarlsson.conveyor.store

import se.gustavkarlsson.conveyor.Action

internal interface Processor<State> {
    suspend fun process(onAction: suspend (Action<State>) -> Unit)
}
