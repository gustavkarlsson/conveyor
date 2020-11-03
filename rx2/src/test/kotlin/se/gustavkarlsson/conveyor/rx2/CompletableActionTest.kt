package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Completable
import kotlinx.coroutines.test.runBlockingTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.rx2.test.SimpleStateAccess
import strikt.api.expectThat

object CompletableActionTest : Spek({
    val stateToSet = "state"
    val stateAccess by memoized { SimpleStateAccess("initial") }
    describe("") {
        val subject by memoized { TrackingCompletableAction(stateToSet) }

        it("a") {
            runBlockingTest {
                subject.execute(stateAccess)
            }
            expectThat(stateAccess)
        }
    }
})

private class TrackingCompletableAction<State : Any>(private val stateToSet: State) : CompletableAction<State>() {
    override fun createCompletable(stateAccess: RxStateAccess<State>): Completable = stateAccess.set(stateToSet)
}
