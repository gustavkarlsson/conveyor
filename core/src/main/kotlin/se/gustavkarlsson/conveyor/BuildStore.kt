package se.gustavkarlsson.conveyor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import se.gustavkarlsson.conveyor.internal.ManualActionsManager
import se.gustavkarlsson.conveyor.internal.LiveActionsManager
import se.gustavkarlsson.conveyor.internal.OpenActionsProcessor
import se.gustavkarlsson.conveyor.internal.StateManager
import se.gustavkarlsson.conveyor.internal.StoreImpl

@ExperimentalCoroutinesApi
@FlowPreview
public fun <State> buildStore(
    initialState: State,
    openActions: Iterable<Action<State>> = emptyList(),
    liveActions: Iterable<Action<State>> = emptyList(),
): Store<State> {
    val stateManager = StateManager(initialState)
    val manualActionsManager = ManualActionsManager<State>()
    val openActionsProcessor = OpenActionsProcessor(openActions)
    val liveActionsManager = LiveActionsManager(liveActions)
    val processors = listOf(manualActionsManager, openActionsProcessor, liveActionsManager)
    val cancellables = listOf(liveActionsManager, manualActionsManager, stateManager)
    return StoreImpl(stateManager, stateManager, manualActionsManager, liveActionsManager, processors, cancellables)
}
