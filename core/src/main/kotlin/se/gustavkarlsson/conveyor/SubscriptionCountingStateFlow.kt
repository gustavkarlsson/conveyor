package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.StateFlow

public interface SubscriptionCountingStateFlow<State> : SubscriptionCounter, StateFlow<State>
