package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.Flow

/**
 * Enables transforming a flows without changing it's type.
 */
public fun interface Transformer<T> {
    /**
     * Transforms the given flow, returning a new flow.
     */
    public fun transform(flow: Flow<T>): Flow<T>
}
