package se.gustavkarlsson.conveyor.rx2.internal

import io.reactivex.Flowable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.rx2.asFlowable
import org.reactivestreams.Subscriber
import se.gustavkarlsson.conveyor.rx2.StateFlowable

@ExperimentalCoroutinesApi
internal class StateFlowableImpl<State : Any>(
    private val state: StateFlow<State>,
) : StateFlowable<State>() {
    private val flowable: Flowable<State> = state.asFlowable()
    override val value: State get() = state.value
    override fun subscribeActual(subscriber: Subscriber<in State>) = flowable.subscribe(subscriber)
}
