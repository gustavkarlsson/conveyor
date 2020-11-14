package se.gustavkarlsson.conveyor

import se.gustavkarlsson.conveyor.internal.ActionManagerImpl
import se.gustavkarlsson.conveyor.internal.StoreImpl
import se.gustavkarlsson.conveyor.internal.UpdatableStateFlowImpl

public fun <State> buildStore(
    initialState: State,
    startActions: Iterable<Action<State>> = emptyList(), // FIXME Test
): Store<State> {
    val updatableStateFlow = UpdatableStateFlowImpl(initialState)
    val actionManager = ActionManagerImpl<State>()
    return StoreImpl(updatableStateFlow, actionManager, startActions)
}
