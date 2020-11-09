package se.gustavkarlsson.conveyor.rx2.internal

import io.reactivex.disposables.Disposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.rx2.RxStore
import se.gustavkarlsson.conveyor.rx2.StateFlowable

@ExperimentalCoroutinesApi
internal class RxStoreImpl<State : Any>(
    private val store: Store<State>,
) : RxStore<State> {
    override val state: StateFlowable<State> = StateFlowableImpl(store.state)

    override fun start(): Disposable {
        val job = store.start(GlobalScope)
        return JobDisposable(job)
    }

    override fun issue(action: Action<State>) = store.issue(action)
}
