package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

public interface RxStateAccess<State : Any> {
    public val state: Flowable<State>
    public fun get(): State
    public fun set(state: State): Completable
    public fun update(block: State.() -> Single<State>): Single<State>
}
