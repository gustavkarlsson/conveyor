package se.gustavkarlsson.conveyor

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

object GlobalConfig : AbstractProjectConfig() {
    override val isolationMode = IsolationMode.InstancePerLeaf
    override val parallelism = getAvailableProcessors()
    override val timeout = 5.seconds
    override val projectTimeout = 1.minutes
}
