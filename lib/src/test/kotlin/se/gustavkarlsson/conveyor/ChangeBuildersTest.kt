package se.gustavkarlsson.conveyor

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.Counter
import se.gustavkarlsson.conveyor.test.FixedStateCommand
import se.gustavkarlsson.conveyor.test.NullAction
import se.gustavkarlsson.conveyor.test.NullCommandIssuer
import se.gustavkarlsson.conveyor.test.TrackingCommandIssuer
import se.gustavkarlsson.conveyor.test.hasCount
import se.gustavkarlsson.conveyor.test.hasIssued
import strikt.api.Assertion
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@ExperimentalCoroutinesApi
object ChangeBuildersTest : Spek({
    val state = "state"
    val command1 = FixedStateCommand("newState1")
    val command2 = FixedStateCommand("newState2")
    val action1 = NullAction<String>()
    val action2 = NullAction<String>()
    val action3 = NullAction<String>()
    val counter by memoized { Counter() }
    val trackingCommandIssuer by memoized { TrackingCommandIssuer<String>() }

    describe("only") {
        it("creates Change with no action") {
            expectThat(state.only()).isEqualTo(Change(state))
        }
    }
    describe("with") {
        it("1 action creates Change with that action") {
            expectThat(state.with(action1)).isEqualTo(Change(state, listOf(action1)))
        }
        it("2 vararg actions creates Change with those actions") {
            expectThat(state.with(action1, action2)).isEqualTo(Change(state, listOf(action1, action2)))
        }
        it("list of actions creates Change with those actions") {
            expectThat(state.with(listOf(action1, action2))).isEqualTo(Change(state, listOf(action1, action2)))
        }
        it("void action creates Change with that action") {
            val change = state.withVoid { counter.increment() }
            change.executeActionsWith(NullCommandIssuer())
            expect {
                that(change).hasNewState(state)
                that(change).hasActionCount(1)
                that(counter).hasCount(1)
            }
        }
        it("single action creates Change with that action") {
            val change = state.withSingle { command1 }
            change.executeActionsWith(trackingCommandIssuer)
            expect {
                that(change).hasNewState(state)
                that(change).hasActionCount(1)
                that(trackingCommandIssuer).hasIssued(command1)
            }
        }
        it("multi action creates Change with that action") {
            val change = state.withMulti {
                issue(command1)
                issue(command2)
            }
            change.executeActionsWith(trackingCommandIssuer)
            expect {
                that(change).hasNewState(state)
                that(change).hasActionCount(1)
                that(trackingCommandIssuer).hasIssued(command1, command2)
            }
        }
        it("flow action creates Change with that action") {
            val change = state.withFlow(flowOf(command1, command2))
            change.executeActionsWith(trackingCommandIssuer)
            expect {
                that(change).hasNewState(state)
                that(change).hasActionCount(1)
                that(trackingCommandIssuer).hasIssued(command1, command2)
            }
        }
    }

    describe("and") {
        val change = Change(state, listOf(action1))
        it("action creates Change with that action added ") {
            expectThat(change.and(action2)).isEqualTo(Change(state, listOf(action1, action2)))
        }
        it("vararg actions creates Change with those actions added") {
            expectThat(change.and(action2, action3)).isEqualTo(Change(state, listOf(action1, action2, action3)))
        }
        it("list of actions creates Change with those actions added") {
            expectThat(change.and(listOf(action2, action3)))
                .isEqualTo(Change(state, listOf(action1, action2, action3)))
        }
        it("void action creates Change with that action added") {
            val newChange = change.andVoid { counter.increment() }
            newChange.executeActionsWith(NullCommandIssuer())
            expect {
                that(newChange).hasNewState(state)
                that(newChange).hasActionCount(2)
                that(counter).hasCount(1)
            }
        }
        it("single action creates Change with that action added") {
            val newChange = change.andSingle { command1 }
            newChange.executeActionsWith(trackingCommandIssuer)
            expect {
                that(newChange).hasNewState(state)
                that(newChange).hasActionCount(2)
                that(trackingCommandIssuer).hasIssued(command1)
            }
        }
        it("multi action creates Change with that action added") {
            val newChange = change.andMulti {
                issue(command1)
                issue(command2)
            }
            newChange.executeActionsWith(trackingCommandIssuer)
            expect {
                that(newChange).hasNewState(state)
                that(newChange).hasActionCount(2)
                that(trackingCommandIssuer).hasIssued(command1, command2)
            }
        }
        it("flow action creates Change with that action added") {
            val newChange = change.andFlow(flowOf(command1, command2))
            newChange.executeActionsWith(trackingCommandIssuer)
            expect {
                that(newChange).hasNewState(state)
                that(newChange).hasActionCount(2)
                that(trackingCommandIssuer).hasIssued(command1, command2)
            }
        }
    }
})

private fun <State> Assertion.Builder<Change<State>>.hasNewState(
    expected: State
): Assertion.Builder<Change<State>> =
    with("newState", { newState }) {
        isEqualTo(expected)
    }

private fun <State> Assertion.Builder<Change<State>>.hasActionCount(
    expected: Int
): Assertion.Builder<Change<State>> =
    with("action count", { actions.size }) {
        isEqualTo(expected)
    }

@ExperimentalCoroutinesApi
private fun <State> Change<State>.executeActionsWith(issuer: CommandIssuer<State>) {
    runBlockingTest {
        for (action in actions) {
            action.execute(issuer)
        }
    }
}
