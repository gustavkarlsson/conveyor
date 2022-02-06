package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Store

internal class StoreImpl<State>(
    stateFlow: StateFlow<State>,
    private val actionIssuer: ActionIssuer<State>,
    private val processes: Iterable<Process>,
) : Store<State> {
    override val state = stateFlow

    private val stageManager = StageManager()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun run(): Nothing {
        stageManager.start()
        try {
            coroutineScope {
                for (process in processes) {
                    launch(start = CoroutineStart.UNDISPATCHED) { process.run() }
                }
                awaitCancellation()
            }
        } catch (t: Throwable) {
            stop(t)
            throw t
        }
    }

    private fun stop(throwable: Throwable) {
        stageManager.stop(throwable)
        actionIssuer.cancel(throwable)
    }

    override fun issue(action: Action<State>) {
        stageManager.requireStarted()
        actionIssuer.issue(action)
    }
}
