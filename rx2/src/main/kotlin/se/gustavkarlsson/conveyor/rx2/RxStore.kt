package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import se.gustavkarlsson.conveyor.Action

@ExperimentalCoroutinesApi
public interface RxStore<State : Any> {
    public val state: Flowable<State>
    public val currentState: State
    public fun start(): Disposable
    public fun issue(action: Action<State>)
    public fun issue(block: (stateAccess: RxStateAccess<State>) -> Completable) {
        issue(completableAction(block))
    }
}
