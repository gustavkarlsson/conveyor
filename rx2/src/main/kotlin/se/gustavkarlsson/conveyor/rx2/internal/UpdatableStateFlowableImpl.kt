package se.gustavkarlsson.conveyor.rx2.internal

import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx2.asFlowable
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxSingle
import org.reactivestreams.Subscriber
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.rx2.StateFlowable
import se.gustavkarlsson.conveyor.rx2.UpdatableStateFlowable

@ExperimentalCoroutinesApi
internal class UpdatableStateFlowableImpl<State : Any>(
    private val state: UpdatableStateFlow<State>,
) : UpdatableStateFlowable<State>() {
    private val flowable: Flowable<State> = state.asFlowable()
    override val value: State get() = state.value
    override fun subscribeActual(subscriber: Subscriber<in State>) = flowable.subscribe(subscriber)
    override fun update(block: State.() -> Single<State>): Single<State> =
        rxSingle(Dispatchers.Unconfined) {
            state.update {
                block().await()
            }
        }

    override val subscriptionCount: StateFlowable<Int> = StateFlowableImpl(state.subscriptionCount)
}
