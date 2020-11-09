package se.gustavkarlsson.conveyor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import se.gustavkarlsson.conveyor.internal.LiveActionsManager
import se.gustavkarlsson.conveyor.internal.ManualActionsManager
import se.gustavkarlsson.conveyor.internal.StateManager
import se.gustavkarlsson.conveyor.internal.StoreImpl

@ExperimentalCoroutinesApi
@FlowPreview
public fun <State> buildStore(
    initialState: State,
    liveActions: Iterable<Action<State>> = emptyList(),
): Store<State> {
    val stateManager = StateManager(initialState)
    val manualActionsManager = ManualActionsManager<State>()
    val liveActionsManager = LiveActionsManager(liveActions, stateManager.subscriptionCount)
    val actionProcessors = listOf(manualActionsManager, liveActionsManager)
    val cancellables = listOf(liveActionsManager, manualActionsManager)

    return StoreImpl(
        stateAccess = stateManager,
        actionIssuer = manualActionsManager,
        actionProcessors = actionProcessors,
        cancellables = cancellables,
    )
}
