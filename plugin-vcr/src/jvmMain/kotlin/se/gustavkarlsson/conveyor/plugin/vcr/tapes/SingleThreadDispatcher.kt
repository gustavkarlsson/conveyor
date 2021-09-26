package se.gustavkarlsson.conveyor.plugin.vcr.tapes

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

internal fun createSingleThreadDispatcher() = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
