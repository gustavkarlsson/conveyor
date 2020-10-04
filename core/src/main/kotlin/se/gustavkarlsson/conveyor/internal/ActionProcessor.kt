package se.gustavkarlsson.conveyor.internal

import se.gustavkarlsson.conveyor.Action

internal interface ActionProcessor<State> {
    suspend fun process(onAction: suspend (Action<State>) -> Unit)
}
