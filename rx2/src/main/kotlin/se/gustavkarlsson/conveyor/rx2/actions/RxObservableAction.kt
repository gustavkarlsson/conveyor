package se.gustavkarlsson.conveyor.rx2.actions

import io.reactivex.Observable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.rx2.asFlow
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.CommandIssuer

public class RxObservableAction<State : Any>(
    private val observable: Observable<Command<State>>,
) : Action<State> {
    @ExperimentalCoroutinesApi
    override suspend fun execute(issuer: CommandIssuer<State>) {
        observable.asFlow().collect { command ->
            issuer.issue(command)
        }
    }
}
