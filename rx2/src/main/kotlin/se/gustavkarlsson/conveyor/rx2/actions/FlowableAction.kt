package se.gustavkarlsson.conveyor.rx2.actions

import io.reactivex.Flowable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactive.asFlow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

public class FlowableAction<State : Any>(
    private val flowable: Flowable<Command<State>>,
) : Action<State> {
    @ExperimentalCoroutinesApi
    override suspend fun execute(issuer: CommandIssuer<State>) {
        flowable.asFlow().collect { command ->
            issuer.issue(command)
        }
    }
}
