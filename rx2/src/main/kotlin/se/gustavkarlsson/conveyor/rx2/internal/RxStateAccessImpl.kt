package se.gustavkarlsson.conveyor.rx2.internal

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx2.asFlowable
import kotlinx.coroutines.rx2.rxCompletable
import kotlinx.coroutines.rx2.rxSingle
import se.gustavkarlsson.conveyor.StateAccess
import se.gustavkarlsson.conveyor.rx2.RxStateAccess

@ExperimentalCoroutinesApi
internal class RxStateAccessImpl<State : Any>(
    private val stateAccess: StateAccess<State>,
) : RxStateAccess<State> {
    override val flowable: Flowable<State> = stateAccess.flow.asFlowable()

    override fun get(): State = stateAccess.get()

    override fun set(state: State): Completable = rxCompletable { stateAccess.set(state) }

    override fun update(block: suspend (currentState: State) -> State): Single<State> =
        rxSingle { stateAccess.update(block) }
}
