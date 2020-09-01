package se.gustavkarlsson.conveyor

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.FixedStateCommand
import se.gustavkarlsson.conveyor.test.NullAction
import se.gustavkarlsson.conveyor.test.NullCommandIssuer
import se.gustavkarlsson.conveyor.test.TrackingCommandIssuer
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo

object ChangeBuildersTest : Spek({
    val state = "state"
    val action1 = NullAction<String>()
    val action2 = NullAction<String>()
    val action3 = NullAction<String>()

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
            var count = 0
            val change = state.withVoid { count++ }
            runBlocking {
                change.actions.forEach { it.execute(NullCommandIssuer()) }
            }
            expect {
                that(change.newState).isEqualTo(state)
                that(count).isEqualTo(1)
            }
        }
        it("single action creates Change with that action") {
            val command = FixedStateCommand("newState")
            val change = state.withSingle { command }
            val commandIssuer = TrackingCommandIssuer<String>()
            runBlocking {
                change.actions.forEach { it.execute(commandIssuer) }
            }
            expect {
                that(change.newState).isEqualTo(state)
                that(commandIssuer.issuedCommands).containsExactly(command)
            }
        }
        it("multi action creates Change with that action") {
            val command1 = FixedStateCommand("newState1")
            val command2 = FixedStateCommand("newState2")
            val change = state.withMulti {
                issue(command1)
                issue(command2)
            }
            val commandIssuer = TrackingCommandIssuer<String>()
            runBlocking {
                change.actions.forEach { it.execute(commandIssuer) }
            }
            expect {
                that(change.newState).isEqualTo(state)
                that(commandIssuer.issuedCommands).containsExactly(command1, command2)
            }
        }
        it("flow action creates Change with that actions") {
            val command1 = FixedStateCommand("newState1")
            val command2 = FixedStateCommand("newState2")
            val change = state.withFlow(flowOf(command1, command2))
            val commandIssuer = TrackingCommandIssuer<String>()
            runBlocking {
                change.actions.forEach { it.execute(commandIssuer) }
            }
            expect {
                that(change.newState).isEqualTo(state)
                that(commandIssuer.issuedCommands).containsExactly(command1, command2)
            }
        }
    }



    describe("and") {
        val change = Change(state, listOf(action1))
        it("1 action creates Change with that action added ") {
            expectThat(change.and(action2)).isEqualTo(Change(state, listOf(action1, action2)))
        }
        it("2 vararg actions creates Change with those actions added") {
            expectThat(change.and(action2, action3)).isEqualTo(Change(state, listOf(action1, action2, action3)))
        }
        it("list of actions creates Change with those actions added") {
            expectThat(change.and(listOf(action2, action3)))
                .isEqualTo(Change(state, listOf(action1, action2, action3)))
        }
        it("void action creates Change with that action added") {
            var count = 0
            val change = change.andVoid { count++ }
            runBlocking {
                change.actions.forEach { it.execute(NullCommandIssuer()) }
            }
            expect {
                that(change.newState).isEqualTo(state)
                that(change.actions).hasSize(2)
                that(count).isEqualTo(1)
            }
        }
        it("single action creates Change with that action added") {
            val command = FixedStateCommand("newState")
            val change = change.andSingle { command }
            val commandIssuer = TrackingCommandIssuer<String>()
            runBlocking {
                change.actions.forEach { it.execute(commandIssuer) }
            }
            expect {
                that(change.newState).isEqualTo(state)
                that(change.actions).hasSize(2)
                that(commandIssuer.issuedCommands).containsExactly(command)
            }
        }
        it("multi action creates Change with that action") {
            val command1 = FixedStateCommand("newState1")
            val command2 = FixedStateCommand("newState2")
            val change = change.andMulti {
                issue(command1)
                issue(command2)
            }
            val commandIssuer = TrackingCommandIssuer<String>()
            runBlocking {
                change.actions.forEach { it.execute(commandIssuer) }
            }
            expect {
                that(change.newState).isEqualTo(state)
                that(change.actions).hasSize(2)
                that(commandIssuer.issuedCommands).containsExactly(command1, command2)
            }
        }
        it("flow action creates Change with that actions") {
            val command1 = FixedStateCommand("newState1")
            val command2 = FixedStateCommand("newState2")
            val change = change.andFlow(flowOf(command1, command2))
            val commandIssuer = TrackingCommandIssuer<String>()
            runBlocking {
                change.actions.forEach { it.execute(commandIssuer) }
            }
            expect {
                that(change.newState).isEqualTo(state)
                that(change.actions).hasSize(2)
                that(commandIssuer.issuedCommands).containsExactly(command1, command2)
            }
        }    }
})
