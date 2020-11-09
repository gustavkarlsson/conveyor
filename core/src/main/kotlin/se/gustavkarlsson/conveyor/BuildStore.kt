package se.gustavkarlsson.conveyor

import se.gustavkarlsson.conveyor.internal.ActionManagerImpl
import se.gustavkarlsson.conveyor.internal.StoreImpl
import se.gustavkarlsson.conveyor.internal.UpdatableStateFlowImpl

public fun <State> buildStore(initialState: State): Store<State> =
    StoreImpl(UpdatableStateFlowImpl(initialState), ActionManagerImpl())
