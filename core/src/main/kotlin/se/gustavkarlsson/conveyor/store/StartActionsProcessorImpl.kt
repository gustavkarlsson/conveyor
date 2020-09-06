package se.gustavkarlsson.conveyor.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.CommandIssuer
import java.util.concurrent.atomic.AtomicReference

internal class StartActionsProcessorImpl<State>(
    actions: Iterable<Action<State>>,
    private val commandIssuer: CommandIssuer<State>,
) : StartActionsProcessor {
    private val actions = AtomicReference(actions.toList())

    override suspend fun process(scope: CoroutineScope) {
        with(consumeActions()) {
            while (hasNext()) {
                val action = next()
                remove()
                scope.launch { action.execute(commandIssuer) }
            }
        }
    }

    private fun consumeActions(): MutableIterator<Action<State>> {
        val actions = checkNotNull(actions.getAndSet(null))
        return actions.toMutableList().iterator()
    }
}
