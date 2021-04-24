package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow

internal class StatefulMutableSharedFlow<T>
private constructor(
    inner: MutableSharedFlow<T>,
    initialValue: T,
) : MutableSharedFlow<T> by inner, StateFlow<T> {
    constructor(initialValue: T) : this(MutableSharedFlow(replay = 1), initialValue)

    init {
        check(inner.tryEmit(initialValue)) { "Initial value rejected" }
    }

    override val value: T
        get() = replayCache.first()
}
