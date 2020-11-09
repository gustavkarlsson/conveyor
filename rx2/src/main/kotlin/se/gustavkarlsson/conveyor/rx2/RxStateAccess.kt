package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Flowable
import io.reactivex.Single

public abstract class RxStateAccess<State : Any> : Flowable<State>() {
    public abstract val currentState: State
    public abstract fun update(block: State.() -> Single<State>): Single<State>
}
