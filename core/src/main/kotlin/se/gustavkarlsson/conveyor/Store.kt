package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

public interface Store<State> {
    public val state: Flow<State>
    public val currentState: State
    public fun start(scope: CoroutineScope): Job
    public fun issue(action: Action<State>)
    public fun issue(block: suspend (stateAccess: StateAccess<State>) -> Unit) {
        issue(action(block))
    }
}
