package se.gustavkarlsson.conveyor.internal

import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow

internal interface ActionManager<State> {
    fun issue(action: Action<State>)
    suspend fun process(stateAccess: UpdatableStateFlow<State>)
    fun cancel(cause: Throwable? = null)
}
