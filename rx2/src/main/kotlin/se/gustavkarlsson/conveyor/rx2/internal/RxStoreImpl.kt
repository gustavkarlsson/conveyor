package se.gustavkarlsson.conveyor.rx2.internal

import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.rx2.asFlowable
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.rx2.RxStore
import kotlin.coroutines.CoroutineContext

// TODO figure out if the context is necessary when creating flowable.
//  Maybe it should be provided ever time we get the state?
@ExperimentalCoroutinesApi
internal class RxStoreImpl<State : Any>(
    private val store: Store<State>,
) : RxStore<State> {
    override val state: Flowable<State> = store.state.asFlowable()

    override val currentState: State get() = store.currentState

    override fun start(): Disposable {
        val job = store.start(GlobalScope)
        return JobDisposable(job)
    }

    override fun issue(action: Action<State>) = store.issue(action)
}
