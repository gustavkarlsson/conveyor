package se.gustavkarlsson.conveyor.internal

import se.gustavkarlsson.conveyor.Action

internal interface ActionIssuer<State> {
    fun issue(action: Action<State>)
    fun cancel(cause: Throwable?)
}
