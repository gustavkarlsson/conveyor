package se.gustavkarlsson.conveyor

public class StoreCancelledException(
    public val reason: Throwable?,
) : IllegalStateException("Store has been cancelled")
