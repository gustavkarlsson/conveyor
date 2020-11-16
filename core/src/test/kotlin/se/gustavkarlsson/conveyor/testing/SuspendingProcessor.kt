package se.gustavkarlsson.conveyor.testing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.internal.Processor

object SuspendingProcessor : Processor {
    override fun process(scope: CoroutineScope): Job = scope.launch { awaitCancellation() }
}
