package se.gustavkarlsson.conveyor.rx2

import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.rx2.internal.RxStoreImpl
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

public fun <State : Any> Store<State>.toRxStore(
    context: CoroutineContext? = null,
): RxStore<State> = RxStoreImpl(this, context)
