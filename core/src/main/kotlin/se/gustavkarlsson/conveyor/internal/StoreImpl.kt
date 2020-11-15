package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.UpdatableStateFlow

internal class StoreImpl<State>(
    private val updatableState: UpdatableStateFlow<State>,
    private val actionManager: ActionManager<State>,
    startActions: Iterable<Action<State>>,
) : Store<State> {
    override val state = updatableState

    private var startActions = startActions.toMutableList()

    private val stage = Stage() // TODO Introduce interface?

    override fun start(scope: CoroutineScope): Job {
        stage.start()
        val job = scope.processActions()
        job.invokeOnCompletion(::stop)
        return job
    }

    private fun CoroutineScope.processActions(): Job = launch {
        val actions = flow {
            emitAll(consumeStartActions())
            emitAll(actionManager.actions)
        }
        actions.collect { action ->
            launch { action.execute(updatableState) }
        }
    }

    private fun consumeStartActions() = flow {
        val actions = startActions.toList()
        startActions.clear()
        for (action in actions) {
            emit(action)
        }
    }

    private fun stop(throwable: Throwable?) {
        stage.stop(throwable)
        actionManager.cancel(throwable)
    }

    override fun issue(action: Action<State>) {
        stage.requireStarted()
        actionManager.issue(action)
    }
}
