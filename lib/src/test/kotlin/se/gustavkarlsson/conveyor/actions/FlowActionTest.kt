package se.gustavkarlsson.conveyor.actions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.FixedStateCommand
import se.gustavkarlsson.conveyor.test.TrackingCommandIssuer
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

@ExperimentalCoroutinesApi
@FlowPreview
object FlowActionTest : Spek({
    val command1 = FixedStateCommand(Unit)
    val command2 = FixedStateCommand(Unit)
    val issuer by memoized { TrackingCommandIssuer<Unit>() }
    val action by memoized {
        FlowAction(flowOf(command1, command2))
    }

    describe("An action") {
        it("does not issue any commands automatically") {
            expectThat(issuer.issuedCommands).isEmpty()
        }
        it("issues all commands when executed") {
            runBlockingTest {
                action.execute(issuer)
            }
            expectThat(issuer.issuedCommands).containsExactly(command1, command2)
        }
        it("issues all commands twice when executed twice") {
            runBlockingTest {
                action.execute(issuer)
                action.execute(issuer)
            }
            expectThat(issuer.issuedCommands)
                .containsExactly(command1, command2, command1, command2)
        }
    }
})
