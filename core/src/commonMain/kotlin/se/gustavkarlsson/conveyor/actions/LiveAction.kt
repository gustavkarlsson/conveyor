package se.gustavkarlsson.conveyor.actions

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.AtomicStateFlow

// FIXME add tests
public abstract class LiveAction<State> : Action<State> {

    final override suspend fun execute(stateFlow: AtomicStateFlow<State>) {
        stateFlow.storeSubscriberCount
            .distinctUntilChangedBy { count ->
                count > 0
            }
            .filter { count ->
                count > 0
            }
            .collectLatest {
                onLive(stateFlow)
            }
    }

    public abstract suspend fun onLive(stateFlow: AtomicStateFlow<State>)
}
