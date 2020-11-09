package se.gustavkarlsson.conveyor.rx2.internal

import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx2.asFlowable
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxSingle
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.rx2.RxStateAccess

@ExperimentalCoroutinesApi
internal class RxStateAccessImpl<State : Any>(
    private val updatableState: UpdatableStateFlow<State>,
) : RxStateAccess<State> {
    override val state: Flowable<State> = updatableState.asFlowable()

    override val currentState: State get() = updatableState.value

    override fun update(block: State.() -> Single<State>): Single<State> =
        rxSingle {
            updatableState.update {
                block().await()
            }
        }
}
