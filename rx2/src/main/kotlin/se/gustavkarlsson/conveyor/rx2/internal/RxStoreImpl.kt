package se.gustavkarlsson.conveyor.rx2.internal

import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.rx2.asFlowable
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.rx2.RxStore

@ExperimentalCoroutinesApi
internal class RxStoreImpl<State : Any>(
    private val store: Store<State>,
) : RxStore<State> {
    override val state: Flowable<State> = store.state.asFlowable()

    override val currentState: State get() = store.state.value

    override fun start(): Disposable {
        val job = store.start(GlobalScope)
        return JobDisposable(job)
    }

    override fun issue(action: Action<State>) = store.issue(action)
}
