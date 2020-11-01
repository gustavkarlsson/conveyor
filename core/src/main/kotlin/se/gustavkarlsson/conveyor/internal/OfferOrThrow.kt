package se.gustavkarlsson.conveyor.internal

import kotlinx.coroutines.channels.SendChannel

internal fun <E> SendChannel<E>.offerOrThrow(element: E) =
    check(offer(element)) { OFFER_OR_THROW_ERROR_MESSAGE }

internal const val OFFER_OR_THROW_ERROR_MESSAGE = "Offer failed, channel over capacity"
