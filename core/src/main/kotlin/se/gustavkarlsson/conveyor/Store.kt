package se.gustavkarlsson.conveyor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

public interface Store<State> : ActionIssuer<State> {
    public val state: StateFlow<State>
    public fun start(scope: CoroutineScope): Job
}

public fun <State> CoroutineScope.start(store: Store<State>): Job =
    store.start(this)


public fun main() {
    val store = buildStore(initialState = "initial")
    with(GlobalScope) {
        // Start processing actions
        val job = start(store)

        // Print state changes
        launch {
            store.state.collect {
                println("State: $it")
            }
        }

        // Issue a simple action that sets the state
        store.issue { state ->
            state.update { "updating" }
        }

        // Issue a more complex action that repeatedly updates the state
        store.issue(RepeatingAppenderAction(append = "."))

        // Run for a while
        runBlocking { delay(5000) }

        // Stop processing actions
        job.cancel()
    }
}

private class RepeatingAppenderAction(
    private val append: String,
) : Action<String> {
    override suspend fun execute(state: UpdatableStateFlow<String>) {
        while (true) {
            delay(1000)
            state.update { this + append }
        }
    }
}
