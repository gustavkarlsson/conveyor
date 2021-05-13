package se.gustavkarlsson.conveyor.actions

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.AtomicStateFlow

public abstract class LiveAction<State> : Action<State> {

    final override suspend fun execute(stateFlow: AtomicStateFlow<State>) {
        stateFlow.storeSubscriberCount
            .map { count -> count > 0 }
            .distinctUntilChanged()
            .collectLatest { live ->
                if (live) {
                    onLive(stateFlow)
                }
            }
    }

    protected abstract suspend fun onLive(stateFlow: AtomicStateFlow<State>)
}
