package se.gustavkarlsson.conveyor.testing

import kotlinx.coroutines.awaitCancellation
import se.gustavkarlsson.conveyor.internal.Process

object SuspendingProcess : Process {
    override suspend fun run() = awaitCancellation()
}
