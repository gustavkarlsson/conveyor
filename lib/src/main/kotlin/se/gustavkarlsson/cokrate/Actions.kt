package se.gustavkarlsson.cokrate

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

public class VoidAction<State : Any>(
    private val block: suspend () -> Unit
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        block()
    }
}

public class SingleAction<State : Any>(
    private val block: suspend () -> Command<State>
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        val command = block()
        issuer.issue(command)
    }
}

public class MultiAction<State : Any>(
    private val block: suspend CommandIssuer<State>.() -> Unit
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        block(issuer)
    }
}

public class FlowAction<State : Any>(
    private val flow: Flow<Command<State>>
) : Action<State> {
    override suspend fun execute(issuer: CommandIssuer<State>) {
        flow.collect(issuer::issue)
    }
}
