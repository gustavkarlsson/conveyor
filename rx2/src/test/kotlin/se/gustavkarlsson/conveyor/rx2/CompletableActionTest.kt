package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Completable
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.rx2.test.SimpleStateAccess
import strikt.api.expectThat
import strikt.assertions.isEqualTo

object CompletableActionTest : Spek({
    val stateToSet = "state"
    val stateAccess by memoized { SimpleStateAccess("initial") }

    describe("An extended CompletableAction") {
        val subject by memoized { TrackingCompletableAction(stateToSet) }

        it("executing works") {
            runBlocking {
                subject.execute(stateAccess)
            }
            expectThat(stateAccess.state.value).isEqualTo(stateToSet)
        }
    }

    describe("A lambda created CompletableAction") {
        val subject by memoized {
            completableAction<String> { stateAccess ->
                stateAccess.set(stateToSet)
            }
        }

        it("executing works") {
            runBlocking {
                subject.execute(stateAccess)
            }
            expectThat(stateAccess.state.value).isEqualTo(stateToSet)
        }
    }
})

private class TrackingCompletableAction<State : Any>(private val stateToSet: State) : CompletableAction<State>() {
    override fun createCompletable(stateAccess: RxStateAccess<State>): Completable = stateAccess.set(stateToSet)
}
