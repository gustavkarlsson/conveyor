package se.gustavkarlsson.conveyor.rx2.actions

import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.rx2.asFlow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

public abstract class ObservableAction<State : Any> : Action<State> {
    @ExperimentalCoroutinesApi
    final override suspend fun execute(issuer: CommandIssuer<State>): Unit =
        observable.asFlow()
            .collect { command ->
                issuer.issue(command)
            }

    protected abstract val observable: Observable<Command<State>>

    public companion object {
        public operator fun <State : Any> invoke(observable: Observable<Command<State>>): ObservableAction<State> =
            ConstructorBlockObservableAction(observable)
    }
}

private class ConstructorBlockObservableAction<State : Any>(
    override val observable: Observable<Command<State>>,
) : ObservableAction<State>()
