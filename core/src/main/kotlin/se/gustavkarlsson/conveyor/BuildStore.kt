package se.gustavkarlsson.conveyor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import se.gustavkarlsson.conveyor.internal.ManualActionsManager
import se.gustavkarlsson.conveyor.internal.StateManager
import se.gustavkarlsson.conveyor.internal.StoreImpl

@ExperimentalCoroutinesApi
@FlowPreview
public fun <State> buildStore(initialState: State): Store<State> {
    val stateManager = StateManager(initialState)
    val manualActionsManager = ManualActionsManager<State>()

    return StoreImpl(
        stateAccess = stateManager,
        actionIssuer = manualActionsManager,
        actionProcessor = manualActionsManager,
        cancellable = manualActionsManager,
    )
}
