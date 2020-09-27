package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import se.gustavkarlsson.conveyor.Command

public interface RxStore<State : Any> {
    public val state: Flowable<State>
    public val currentState: State
    public fun open(): Disposable
    public fun issue(command: Command<State>)
}
