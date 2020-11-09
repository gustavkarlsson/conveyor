package se.gustavkarlsson.conveyor

import se.gustavkarlsson.conveyor.internal.ManualActionsManager
import se.gustavkarlsson.conveyor.internal.StateManager
import se.gustavkarlsson.conveyor.internal.StoreImpl

public fun <State> buildStore(initialState: State): Store<State> =
    StoreImpl(StateManager(initialState), ManualActionsManager())
