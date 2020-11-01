package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import se.gustavkarlsson.conveyor.Action

public interface RxStore<State : Any> {
    public val state: Flowable<State>
    public val currentState: State
    public fun start(): Disposable
    public fun issue(action: Action<State>)
}
