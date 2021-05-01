package se.gustavkarlsson.conveyor

/**
 * Marks declarations that are **internal** in conveyor, which means that should not be used from outside of conveyor,
 * because their signatures and semantics will change between future releases without any
 * warnings and without providing any migration aids.
 */
@Retention(value = AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPEALIAS, AnnotationTarget.PROPERTY)
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR, message = "This is an internal conveyor API that " +
        "should not be used from outside of conveyor. No compatibility guarantees are provided."
)
public annotation class InternalConveyorApi
