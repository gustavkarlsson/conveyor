package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import se.gustavkarlsson.conveyor.ActionIssuer

public interface RxStore<State : Any> : ActionIssuer<State> {
    public val state: StateFlowable<State>
    public fun start(): Disposable
}

@ExperimentalCoroutinesApi
public fun <State : Any> ActionIssuer<State>.issue(
    block: (state: UpdatableStateFlowable<State>) -> Completable,
): Unit = issue(completableAction(block))
