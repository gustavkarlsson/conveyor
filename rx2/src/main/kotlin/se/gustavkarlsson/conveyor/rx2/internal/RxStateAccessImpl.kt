package se.gustavkarlsson.conveyor.rx2.internal

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx2.asFlowable
import kotlinx.coroutines.rx2.await
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import se.gustavkarlsson.conveyor.StateAccess
import se.gustavkarlsson.conveyor.rx2.RxStateAccess

@ExperimentalCoroutinesApi
internal class RxStateAccessImpl<State : Any>(
    private val stateAccess: StateAccess<State>,
) : RxStateAccess<State> {
    override val state: Flowable<State> = stateAccess.state.asFlowable()

    override fun get(): State = stateAccess.state.value

    override fun set(state: State): Completable = rxCompletable { stateAccess.set(state) }

    override fun update(block: Single<State>.() -> Single<State>): Single<State> =
        rxSingle {
            stateAccess.update {
                Single.just(this).block().await()
            }
        }
}
