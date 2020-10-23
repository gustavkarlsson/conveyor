package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.Flow

public fun interface Transformer<T > {
    public fun transform(flow: Flow<T>): Flow<T>
}
