package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.Flow

internal typealias Transformer<T> = (Flow<T>) -> Flow<T>
