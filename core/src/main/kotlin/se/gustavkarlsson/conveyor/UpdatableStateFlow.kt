package se.gustavkarlsson.conveyor

public interface UpdatableStateFlow<State> : SubscriptionCountingStateFlow<State>, StateUpdater<State>
