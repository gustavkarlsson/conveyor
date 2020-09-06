package se.gustavkarlsson.conveyor

import kotlinx.coroutines.channels.SendChannel

internal fun <E> SendChannel<E>.offerOrThrow(element: E) =
    check(offer(element)) {
        "Offer failed, channel over capacity"
    }
