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
    val actionProcessors = listOf(manualActionsManager)
    val cancellables = listOf(manualActionsManager)

    return StoreImpl(
        stateAccess = stateManager,
        actionIssuer = manualActionsManager,
        actionProcessors = actionProcessors,
        cancellables = cancellables,
    )
}
