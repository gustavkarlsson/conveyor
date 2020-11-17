package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Store

internal class StoreImpl<State>(
    stateFlow: StateFlow<State>,
    private val actionManager: ActionManager<State>,
    private val processors: Iterable<Processor>,
) : Store<State> {
    override val state = stateFlow

    private val stage = Stage() // TODO Introduce interface?

    override fun start(scope: CoroutineScope): Job {
        stage.start()
        val job = scope.launch {
            processors.map { processor ->
                processor.process(scope)
            }.joinAll()
        }
        job.invokeOnCompletion(::stop)
        return job
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
