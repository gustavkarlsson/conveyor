package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import se.gustavkarlsson.conveyor.Action
import java.util.concurrent.atomic.AtomicReference

internal class StartActionFlowProvider<State>(
    actions: Iterable<Action<State>>,
) : ActionFlowProvider<State> {
    private val actions = AtomicReference(actions.toList())

    override val actionFlow: Flow<Action<State>> = flow {
        with(consumeActions()) {
            while (hasNext()) {
                val action = next()
                remove()
                emit(action)
            }
        }
    }

    private fun consumeActions(): MutableIterator<Action<State>> {
        val actions = checkNotNull(actions.getAndSet(null))
        return actions.toMutableList().iterator()
    }
}
