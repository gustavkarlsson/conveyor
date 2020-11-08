# Conveyor

[![Checks](https://github.com/gustavkarlsson/conveyor/workflows/Checks/badge.svg?branch=master)](https://github.com/gustavkarlsson/conveyor/actions?query=workflow%3AChecks+branch%3Amaster+event%3Apush)
[![codecov](https://codecov.io/gh/gustavkarlsson/conveyor/branch/master/graph/badge.svg)](https://codecov.io/gh/gustavkarlsson/conveyor)
[![JitPack](https://jitpack.io/v/gustavkarlsson/conveyor.svg)](https://jitpack.io/#gustavkarlsson/conveyor)
[![MIT license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/gustavkarlsson/krate/blob/master/LICENSE.md)

A pragmatic and predictable state container utilizing kotlin coroutines.

Heavily inspired by [beworker/knot](https://github.com/beworker/knot) :heart:

## Example
```kotlin
// Create a store with an initial state
val store = buildStore(initialState = 0)
with(GlobalScope) {
    // Start processing actions.
    val job = start(store)

    // Subscribe to state updates and print them
    launch {
        store.state.collect { println("State: $it") }
    }

    // Issue a simple action that sets the state
    store.issue { stateAccess ->
        stateAccess.set(1)
    }

    // Issue a more complex action that increments the state
    store.issue(IncrementAction(count = 3, increment = 2))

    // Run for a while
    runBlocking { delay(10000) }

    // Stop processing actions
    job.cancel()
}

/*
Output:
State: 0
State: 1
State: 3
State: 5
State: 7
*/
```

## Downloading

The library is still under heavy development, but you are welcome to try a `SNAPSHOT` version!

To add **Conveyor** to your project, make sure you have added the Jitpack repository:

````kotlin
repositories {
  maven(url = "https://jitpack.io")
}
````

Then add the following dependency to your gradle build file:

```kotlin
dependencies {
    implementation("com.github.gustavkarlsson.conveyor:conveyor-core:master-SNAPSHOT") // Main library
    // implementation("com.github.gustavkarlsson.conveyor:conveyor-rx2:master-SNAPSHOT") // RxJava 2 support
}
```
