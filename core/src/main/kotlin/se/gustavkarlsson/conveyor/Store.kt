package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow

public interface Store<State> {
    public val state: StateFlow<State>
    public fun start(scope: CoroutineScope): Job
    public fun issue(action: Action<State>)
}

public fun <State> Store<State>.issue(
    block: suspend (stateAccess: StateAccess<State>) -> Unit,
): Unit = issue(action(block))
