package se.gustavkarlsson.conveyor.store

import se.gustavkarlsson.conveyor.Action
import java.util.concurrent.atomic.AtomicReference

internal class OpenActionsProcessor<State>(
    actions: Iterable<Action<State>>,
) : Processor<State> {
    private val actions = AtomicReference(actions.toList())

    override suspend fun process(onAction: (Action<State>) -> Unit) {
        with(consumeActions()) {
            while (hasNext()) {
                val action = next()
                remove()
                onAction(action)
            }
        }
    }

    private fun consumeActions(): MutableIterator<Action<State>> {
        val actions = checkNotNull(actions.getAndSet(null))
        return actions.toMutableList().iterator()
    }
}
