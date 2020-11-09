package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.Store

@FlowPreview
@ExperimentalCoroutinesApi
internal class StoreImpl<State>(
    private val stateAccess: UpdatableStateFlow<State>,
    private val actionManager: ActionManager<State>,
) : Store<State> {
    override val state = stateAccess

    private val stage = Stage() // TODO Introduce interface?

    override fun start(scope: CoroutineScope): Job {
        stage.start()
        val job = scope.processActions()
        job.invokeOnCompletion(::stop)
        return job
    }

    private fun CoroutineScope.processActions(): Job = launch {
        actionManager.actions.collect { action ->
            launch { action.execute(stateAccess) }
        }
        awaitCancellation()
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
