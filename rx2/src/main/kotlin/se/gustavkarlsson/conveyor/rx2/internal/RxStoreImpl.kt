package se.gustavkarlsson.conveyor.rx2.internal

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.rx2.asFlowable
import kotlinx.coroutines.rx2.rxCompletable
import se.gustavkarlsson.conveyor.Command
import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.rx2.RxStore
import kotlin.coroutines.CoroutineContext

internal class RxStoreImpl<State : Any>(
    private val store: Store<State>,
    context: CoroutineContext?,
) : RxStore<State> {
    @ExperimentalCoroutinesApi
    override val state: Flowable<State> =
        if (context == null) {
            store.state.asFlowable()
        } else {
            store.state.asFlowable(context)
        }

    override val currentState: State get() = store.currentState

    override fun open(): Disposable {
        val job = store.open(GlobalScope)
        return JobDisposable(job)
    }

    override fun issue(command: Command<State>): Completable =
        rxCompletable { store.issue(command) }
}
