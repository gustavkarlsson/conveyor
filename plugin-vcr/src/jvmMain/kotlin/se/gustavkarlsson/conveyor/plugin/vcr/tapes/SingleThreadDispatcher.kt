package se.gustavkarlsson.conveyor.plugin.vcr.tapes

import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors

// FIXME function instead?
internal val SingleThreadDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
