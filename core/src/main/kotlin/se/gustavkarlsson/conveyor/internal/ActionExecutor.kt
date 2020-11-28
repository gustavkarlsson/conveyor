package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Transformer
import se.gustavkarlsson.conveyor.UpdatableStateFlow

internal class ActionExecutor<State>(
    startActions: Iterable<Action<State>>,
    private val actions: Flow<Action<State>>,
    private val transformers: Iterable<Transformer<Action<State>>>,
    private val state: UpdatableStateFlow<State>,
) : Launcher {
    private var startActions: List<Action<State>>? = startActions.toList()

    override fun launch(scope: CoroutineScope): Job = scope.launch {
        val actions = flow {
            emitAll(consumeStartActionsAsFlow())
            emitAll(actions)
        }
        actions
            .transform(transformers)
            .collect { action ->
                launch { action.execute(state) }
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
