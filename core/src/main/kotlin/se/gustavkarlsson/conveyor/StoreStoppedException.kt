package se.gustavkarlsson.conveyor

public class StoreStoppedException(
    public val cancellationReason: Throwable?,
) : IllegalStateException(createMessage(cancellationReason))

private fun createMessage(reason: Throwable?) =
    if (reason == null) {
        "Store has been stopped"
    } else {
        "Store has been stopped due to cancellation"
    }
