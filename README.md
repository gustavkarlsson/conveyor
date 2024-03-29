# Conveyor

[![Checks](https://github.com/gustavkarlsson/conveyor/workflows/Checks/badge.svg?branch=master)](https://github.com/gustavkarlsson/conveyor/actions?query=workflow%3AChecks+branch%3Amaster+event%3Apush)
[![codecov](https://codecov.io/gh/gustavkarlsson/conveyor/branch/master/graph/badge.svg)](https://codecov.io/gh/gustavkarlsson/conveyor)
[![JitPack](https://jitpack.io/v/gustavkarlsson/conveyor.svg)](https://jitpack.io/#gustavkarlsson/conveyor)
[![MIT license](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/gustavkarlsson/krate/blob/master/LICENSE.md)

A pragmatic and predictable state container utilizing kotlin coroutines.

Heavily inspired by [beworker/knot](https://github.com/beworker/knot) :heart:

## Example
```kotlin
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.StoreFlow
import se.gustavkarlsson.conveyor.issue

suspend fun main() {
    val store = Store(initialState = 0)
    coroutineScope {
        // Start processing actions
        launch {
            store.run()
        }

        // Print state changes
        launch {
            store.state.collect {
                println("State: $it")
            }
        }

        // Issue a simple action that sets the state
        store.issue { storeFlow ->
            storeFlow.update { 100 }
        }

        // Issue a more complex action that repeatedly updates the state
        store.issue(RepeatingIncrementAction(increment = 1))

        // Run for a while
        delay(5000)

        // Cancel the scope, and the store with it
        cancel()
    }
}

private class RepeatingIncrementAction(
    private val increment: Int,
) : Action<Int> {
    override suspend fun execute(storeFlow: StoreFlow<Int>) {
        while (true) {
            delay(1000)
            storeFlow.update { state ->
                state + increment
            }
        }
    }
}
```

Outputs:

```
State: 0
State: 100
State: 101
State: 102
State: 103
State: 104
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
}
```
