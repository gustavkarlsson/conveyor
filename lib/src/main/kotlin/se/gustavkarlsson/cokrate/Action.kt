package se.gustavkarlsson.cokrate

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

public interface Action<State : Any> {
    public suspend fun execute(issue: suspend (Command<State>) -> Unit)
}

public class SingleAction<State : Any>(
    private val block: suspend () -> Command<State>
) : Action<State> {
    override suspend fun execute(issue: suspend (Command<State>) -> Unit) {
        val command = block()
        issue(command)
    }
}

public class MultiAction<State : Any>(
    private val collect: suspend (issue: suspend (Command<State>) -> Unit) -> Unit
) : Action<State> {
    override suspend fun execute(issue: suspend (Command<State>) -> Unit) {
        collect(issue)
    }
}

public class FlowAction<State : Any>(
    private val flow: Flow<Command<State>>
) : Action<State> {
    override suspend fun execute(issue: suspend (Command<State>) -> Unit) {
        flow.collect(issue)
    }
}
