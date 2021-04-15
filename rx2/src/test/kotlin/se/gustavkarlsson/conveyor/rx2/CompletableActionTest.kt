package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Completable
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.testing.SimpleUpdatableStateFlow
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object CompletableActionTest : Spek({
    val stateToSet = "state"
    val stateFlow by memoized { SimpleUpdatableStateFlow("initial") }

    describe("A tracking CompletableAction") {
        val subject by memoized { TrackingCompletableAction(stateToSet) }

        it("executing works") {
            runBlocking {
                subject.execute(stateFlow)
            }
            expectThat(stateFlow.value).isEqualTo(stateToSet)
        }
    }

    describe("A lambda created CompletableAction") {
        val subject by memoized {
            CompletableAction<String> { state ->
                state
                    .update { stateToSet }
                    .ignoreElement()
            }
        }

        it("executing works") {
            runBlocking {
                subject.execute(stateFlow)
            }
            expectThat(stateFlow.value).isEqualTo(stateToSet)
        }
    }
})

private class TrackingCompletableAction<State : Any>(private val stateToSet: State) : CompletableAction<State>() {
    override fun execute(stateFlow: UpdatableStateFlowable<State>): Completable =
        stateFlow
            .update { stateToSet }
            .ignoreElement()
}
