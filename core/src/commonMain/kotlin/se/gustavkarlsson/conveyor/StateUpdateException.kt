package se.gustavkarlsson.conveyor

/**
 * Indicates that the state could not be updated. See [state] for the previous state
 */
public class StateUpdateException internal constructor(
    public val state: Any?,
    public override val cause: Throwable,
) : IllegalStateException(buildMessage(state), cause) {
    override val message: String get() = super.message!!

    private companion object {
        fun buildMessage(state: Any?): String = "Failed to update the state: $state"
    }
}
