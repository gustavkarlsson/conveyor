package se.gustavkarlsson.conveyor.internal

internal interface Process {
    suspend fun run()
}
