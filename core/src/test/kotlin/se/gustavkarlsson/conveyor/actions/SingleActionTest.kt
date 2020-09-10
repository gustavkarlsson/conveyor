package se.gustavkarlsson.conveyor.actions

import kotlinx.coroutines.test.runBlockingTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.test.FixedStateCommand
import se.gustavkarlsson.conveyor.test.TrackingCommandIssuer
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

object SingleActionTest : Spek({
    val command = FixedStateCommand(Unit)
    val issuer by memoized { TrackingCommandIssuer<Unit>() }
    val subject = SingleAction { command }

    describe("An action") {
        it("does not issue any commands automatically") {
            expectThat(issuer.issuedCommands).isEmpty()
        }
        it("issues command when executed") {
            runBlockingTest {
                subject.execute(issuer)
            }
            expectThat(issuer.issuedCommands).containsExactly(command)
        }
        it("issues commands twice when executed twice") {
            runBlockingTest {
                subject.execute(issuer)
                subject.execute(issuer)
            }
            expectThat(issuer.issuedCommands).containsExactly(command, command)
        }
    }
})
