package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.Flow

internal fun <T> Flow<T>.transform(transformers: Iterable<Transformer<T>>) =
    transformers.fold(this) { acc, transformer ->
        transformer(acc)
    }
