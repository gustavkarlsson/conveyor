package se.gustavkarlsson.conveyor

public class StoreClosedException(public val reason: Throwable?) : IllegalStateException("Store has closed" )
