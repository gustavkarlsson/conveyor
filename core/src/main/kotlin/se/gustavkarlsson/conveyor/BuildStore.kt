package se.gustavkarlsson.conveyor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import se.gustavkarlsson.conveyor.store.CommandManager
import se.gustavkarlsson.conveyor.store.LiveActionsManager
import se.gustavkarlsson.conveyor.store.OpenActionsProcessor
import se.gustavkarlsson.conveyor.store.StateManager
import se.gustavkarlsson.conveyor.store.StoreImpl

@ExperimentalCoroutinesApi
@FlowPreview
public fun <State> buildStore(
    initialState: State,
    openActions: Iterable<Action<State>> = emptyList(),
    liveActions: Iterable<Action<State>> = emptyList(),
    commandBufferSize: Int = 64,
): Store<State> {
    val stateManager = StateManager(initialState)
    val commandManager = CommandManager(commandBufferSize, { stateManager.state }, { stateManager.state })
    val openActionsProcessor = OpenActionsProcessor(openActions, commandManager)
    val liveActionsManager = LiveActionsManager(liveActions, commandManager)
    val processors = listOf(commandManager, openActionsProcessor, liveActionsManager)
    val cancellables = listOf(liveActionsManager, commandManager, stateManager)
    return StoreImpl(stateManager, commandManager, liveActionsManager, processors, cancellables)
}
