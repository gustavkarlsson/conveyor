package se.gustavkarlsson.conveyor

public class StoreStoppedException(
    public val cancellationReason: Throwable?,
) : IllegalStateException("Store has been stopped")
