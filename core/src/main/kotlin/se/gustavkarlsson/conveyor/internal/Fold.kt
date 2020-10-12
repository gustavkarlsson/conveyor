package se.gustavkarlsson.conveyor.internal

import se.gustavkarlsson.conveyor.Mapper

internal fun <T> Iterable<Mapper<T>>.fold(initial: T?): T? =
    fold(initial) { acc, mapper ->
        if (acc != null) {
            mapper.map(acc)
        } else {
            acc
        }
    }
