package se.gustavkarlsson.conveyor

/**
 * Indicates that a store has not been started when it is required to be.
 */
public class StoreNotYetStartedException : IllegalStateException("Store has not yet been started")

/**
 * Indicates that a store has already been started when it is required not to be.
 */
public class StoreAlreadyStartedException : IllegalStateException("Store has already been started")

/**
 * Indicates that a store has been stopped when it is required not to be.
 */
public class StoreStoppedException(
    public val cancellationReason: Throwable?,
) : IllegalStateException(createMessage(cancellationReason))

private fun createMessage(reason: Throwable?) =
    if (reason == null) {
        "Store has been stopped"
    } else {
        "Store has been stopped due to cancellation"
    }
