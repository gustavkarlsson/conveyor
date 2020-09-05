package se.gustavkarlsson.conveyor.changebuilders

@RequiresOptIn(message = "This API is experimental. To ensure stability, use a Change constructor instead.")
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
public annotation class ExperimentalChangeBuilder

// TODO consider visibility of the extensions.
//  Should they be encapsulated in a DSL?
//  What about state.with(action).with(action) returning the wrong type?
