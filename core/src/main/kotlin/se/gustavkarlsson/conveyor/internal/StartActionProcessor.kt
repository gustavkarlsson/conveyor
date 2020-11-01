package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess

internal class StartActionProcessor<State>(
    actions: Iterable<Action<State>>,
) : ActionProcessor<State> {
    private val actions = Consumable(actions.toList())

    override suspend fun process(stateAccess: StateAccess<State>) {
        val actions = actions.consume()
        coroutineScope {
            for (action in actions) {
                launch { action.execute(stateAccess) }
            }
        }
    }
}
