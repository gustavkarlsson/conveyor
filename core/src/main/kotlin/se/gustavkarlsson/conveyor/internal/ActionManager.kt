package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.Flow
import se.gustavkarlsson.conveyor.Action

internal interface ActionManager<State> {
    val actions: Flow<Action<State>>
    fun issue(action: Action<State>)
    fun cancel(cause: Throwable? = null)
}
