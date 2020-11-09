package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.StateFlow

public interface SubscriptionCountingStateFlow<State> : StateFlow<State> {
    public val subscriptionCount: StateFlow<Int>
}
