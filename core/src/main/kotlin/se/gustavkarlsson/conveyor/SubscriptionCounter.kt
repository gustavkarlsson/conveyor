package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.StateFlow

public interface SubscriptionCounter {
    public val subscriptionCount: StateFlow<Int>
}
