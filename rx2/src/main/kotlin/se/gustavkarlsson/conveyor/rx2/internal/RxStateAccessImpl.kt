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
    private val stateAccess: UpdatableStateFlow<State>,
) : RxStateAccess<State> {
    override val state: Flowable<State> = stateAccess.asFlowable()

    override val currentState: State get() = stateAccess.value

    override fun update(block: State.() -> Single<State>): Single<State> =
        rxSingle {
            stateAccess.update {
                block().await()
            }
        }
}
