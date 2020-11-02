package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.StateAccess
import strikt.api.expectThat

object CompletableActionTest : Spek({
    val stateToSet = "state"
    val stateAccess by memoized { SimpleStateAccess("initial") }
    describe("") {
        val subject by memoized { TrackingCompletableAction(stateToSet) }

        it("") {
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

private class SimpleStateAccess<State>(initialState: State) : StateAccess<State> {
    private val mutableStateFlow = MutableStateFlow(initialState)

    override val flow: Flow<State> = mutableStateFlow

    override fun get(): State = mutableStateFlow.value

    override suspend fun set(state: State) {
        mutableStateFlow.value = state
    }

    override suspend fun update(block: suspend (currentState: State) -> State): State {
        val newValue = block(mutableStateFlow.value)
        set(newValue)
        return newValue
    }
}
