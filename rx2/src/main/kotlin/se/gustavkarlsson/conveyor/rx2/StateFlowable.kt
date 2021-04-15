package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Flowable
import kotlinx.coroutines.flow.StateFlow

/**
 * An RxJava version of [StateFlow].
 */
public abstract class StateFlowable<State : Any> : Flowable<State>() {
    /**
     * The current value of this state flowable.
     */
    public abstract val value: State
}
