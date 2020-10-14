package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.Flow

public interface Transformer<T > {
    public suspend fun transform(flow: Flow<T>): Flow<T>
}
