package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

public interface Store<State> {
    public fun start(scope: CoroutineScope): Job
    public val state: Flow<State>
    public suspend fun issue(command: Command<State>)
}
