package se.gustavkarlsson.cokrate.actions

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.cokrate.actions.SingleAction
import se.gustavkarlsson.cokrate.test.FixedStateCommand
import se.gustavkarlsson.cokrate.test.TrackingCommandIssuer
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

@ExperimentalCoroutinesApi
@FlowPreview
object SingleActionTest : Spek({
    val command = FixedStateCommand(Unit)
    val issuer by memoized { TrackingCommandIssuer<Unit>() }
    val action by memoized { SingleAction { command } }

    describe("An action") {
        it("does not issue any commands automatically") {
            expectThat(issuer.issuedCommands).isEmpty()
        }
        it("issues command when executed") {
            runBlocking {
                action.execute(issuer)
            }
            expectThat(issuer.issuedCommands).containsExactly(command)
        }
        it("issues commands twice when executed twice") {
            runBlocking {
                action.execute(issuer)
                action.execute(issuer)
            }
            expectThat(issuer.issuedCommands).containsExactly(command, command)
        }
    }
})
