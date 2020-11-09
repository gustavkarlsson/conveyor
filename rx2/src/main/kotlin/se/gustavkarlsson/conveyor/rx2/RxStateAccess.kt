package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Flowable
import io.reactivex.Single

// TODO Rework to Flowable
public interface RxStateAccess<State : Any> {
    public val state: Flowable<State>
    public val currentState: State
    public fun update(block: State.() -> Single<State>): Single<State>
}
