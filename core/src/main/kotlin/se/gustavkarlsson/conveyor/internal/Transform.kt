package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.flow.Flow
import se.gustavkarlsson.conveyor.Transformer

internal fun <T> Flow<T>.transform(transformers: Iterable<Transformer<T>>) =
    transformers.fold(this) { acc, transformer ->
        transformer.transform(acc)
    }
