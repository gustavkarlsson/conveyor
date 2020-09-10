package se.gustavkarlsson.conveyor.rx2.actions

import io.reactivex.Flowable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactive.asFlow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

public abstract class FlowableAction<State : Any> : Action<State> {
    final override suspend fun execute(issuer: CommandIssuer<State>): Unit =
        flowable.asFlow()
            .collect { command ->
                issuer.issue(command)
            }

    protected abstract val flowable: Flowable<Command<State>>

    public companion object {
        public operator fun <State : Any> invoke(flowable: Flowable<Command<State>>): Action<State> =
            ConstructorBlockFlowableAction(flowable)
    }
}

private class ConstructorBlockFlowableAction<State : Any>(
    override val flowable: Flowable<Command<State>>,
) : FlowableAction<State>()
