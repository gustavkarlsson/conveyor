package se.gustavkarlsson.conveyor.rx2.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.Store
import se.gustavkarlsson.conveyor.action
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.first
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isTrue

object RxStoreImplTest : Spek({
    val state = "state"
    val innerStore by memoized { FakeStore(state) }
    val action = action<String> {}

    describe("An RxStoreImpl") {
        val subject by memoized { RxStoreImpl(innerStore) }

        it("state.value gets state") {
            val currentState = subject.state.value
            expectThat(currentState).isEqualTo(state)
        }
        it("state gets state") {
            val firstElement = subject.state.blockingFirst()
            expectThat(firstElement).isEqualTo(state)
        }
        it("issue issues action") {
            subject.issue(action)
            expectThat(innerStore.issuedActions).containsExactly(action)
        }
        it("start returns disposable connected to job") {
            val disposable = subject.start()
            disposable.dispose()
            expectThat(innerStore.startedJobs)
                .describedAs("started jobs")
                .hasSize(1)
                .first().get { isCancelled }.isTrue()
        }
    }
})

private class FakeStore<State>(initialState: State) : Store<State> {
    override val state: StateFlow<State> = MutableStateFlow(initialState)

    private val _startedJobs = mutableListOf<Job>()
    val startedJobs: List<Job> = _startedJobs

    override fun start(scope: CoroutineScope): Job {
        val job = scope.launch { awaitCancellation() }
        _startedJobs += job
        return job
    }

    private val _issuedActions = mutableListOf<Action<State>>()
    val issuedActions: List<Action<State>> = _issuedActions

    override fun issue(action: Action<State>) {
        _issuedActions += action
    }
}
