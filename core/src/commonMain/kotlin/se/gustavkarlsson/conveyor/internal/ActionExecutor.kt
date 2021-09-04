package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.StoreFlow

internal class ActionExecutor<State>(
    startActions: Iterable<Action<State>>,
    private val actions: Flow<Action<State>>,
    private val transformers: Iterable<Transformer<Action<State>>>,
    private val storeFlow: StoreFlow<State>,
) : Process {
    private var startActions: List<Action<State>>? = startActions.toList()

    override suspend fun run() {
        val actions = flow {
            emitAll(consumeStartActionsAsFlow())
            emitAll(actions)
        }
        coroutineScope {
            actions
                .transform(transformers)
                .collect { action ->
                    launch { action.execute(storeFlow) }
                }
        }
    }

    private fun consumeStartActionsAsFlow(): Flow<Action<State>> = flow {
        val actions = checkNotNull(startActions) { "Already consumed" }
        startActions = null
        for (action in actions) {
            emit(action)
        }
    }
}
