package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StateAccess
import java.util.concurrent.atomic.AtomicReference

internal class StartActionProcessor<State>(
    actions: Iterable<Action<State>>,
) : ActionProcessor<State> {
    private val actions = AtomicReference(actions.toList())

    override suspend fun process(stateAccess: StateAccess<State>) {
        val actions = checkNotNull(actions.getAndSet(null))
        supervisorScope { // TODO is this extra scope needed?
            actions.map { action ->
                launch { action.execute(stateAccess) }
            }
        }
    }
}
