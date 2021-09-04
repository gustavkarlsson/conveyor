package se.gustavkarlsson.conveyor.actions

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreFlow

public abstract class LiveAction<State> : Action<State> {

    final override suspend fun execute(storeFlow: StoreFlow<State>) {
        storeFlow.storeSubscriberCount
            .map { count -> count > 0 }
            .distinctUntilChanged()
            .collectLatest { live ->
                if (live) {
                    onLive(storeFlow)
                }
            }
    }

    protected abstract suspend fun onLive(storeFlow: StoreFlow<State>)
}
