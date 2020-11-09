package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Flowable

public abstract class StateFlowable<State : Any> : Flowable<State>() {
    public abstract val value: State
}
