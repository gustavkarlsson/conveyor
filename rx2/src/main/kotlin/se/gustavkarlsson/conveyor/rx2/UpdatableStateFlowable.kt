package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Single

public abstract class UpdatableStateFlowable<State : Any> : StateFlowable<State>() {
    public abstract fun update(block: State.() -> Single<State>): Single<State>
    public abstract val subscriptionCount: StateFlowable<Int>
}
