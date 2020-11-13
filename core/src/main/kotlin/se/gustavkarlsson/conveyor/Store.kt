package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

public interface Store<State> : ActionIssuer<State> {
    public val state: StateFlow<State>
    public fun start(scope: CoroutineScope): Job
}

public fun <State> CoroutineScope.start(store: Store<State>): Job =
    store.start(this)
