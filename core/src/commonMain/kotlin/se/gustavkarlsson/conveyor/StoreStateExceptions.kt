package se.gustavkarlsson.conveyor

/**
 * Indicates that a store has not been started when it is required to be.
 */
public class StoreNotYetStartedException internal constructor() :
    IllegalStateException("Store has not yet been started") {
    override val message: String get() = super.message!!
}

/**
 * Indicates that a store has already been started when it is required not to be.
 */
public class StoreAlreadyStartedException internal constructor() :
    IllegalStateException("Store has already been started") {
    override val message: String get() = super.message!!
}

/**
 * Indicates that a store has been stopped when it is required not to be.
 */
public class StoreStoppedException internal constructor(
    public val cancellationReason: Throwable,
) : IllegalStateException("Store has been stopped due to cancellation") {
    override val message: String get() = super.message!!
}
