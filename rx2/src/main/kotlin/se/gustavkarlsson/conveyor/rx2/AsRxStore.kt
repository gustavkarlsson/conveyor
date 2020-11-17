package se.gustavkarlsson.conveyor.rx2

import kotlinx.coroutines.ExperimentalCoroutinesApi
import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.rx2.internal.RxStoreImpl

@ExperimentalCoroutinesApi
public fun <State : Any> Store<State>.asRxStore(): RxStore<State> = RxStoreImpl(this)
