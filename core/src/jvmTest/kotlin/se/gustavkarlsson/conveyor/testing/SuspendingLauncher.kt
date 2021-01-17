package se.gustavkarlsson.conveyor.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.internal.Launcher

object SuspendingLauncher : Launcher {
    override fun launch(scope: CoroutineScope): Job = scope.launch { awaitCancellation() }
}
