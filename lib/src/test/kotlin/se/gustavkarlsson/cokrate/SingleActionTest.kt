package se.gustavkarlsson.cokrate

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEmpty

@ExperimentalCoroutinesApi
@FlowPreview
object SingleActionTest : Spek({
    val command = Command<Unit> { Change(Unit) }
    val issuedCommands by memoized { mutableListOf<Command<Unit>>() }
    val issuer = object : CommandIssuer<Unit> {
        override suspend fun issue(command: Command<Unit>) {
            issuedCommands.add(command)
        }
    }
    val action by memoized { SingleAction { command } }

    describe("An action") {
        it("does not run block automatically") {
            expectThat(issuedCommands).isEmpty()
        }
        it("issues command when executed") {
            runBlocking {
                action.execute(issuer)
            }
            expectThat(issuedCommands).containsExactly(command)
        }
        it("issues commands twice when executed twice") {
            runBlocking {
                action.execute(issuer)
                action.execute(issuer)
            }
            expectThat(issuedCommands).containsExactly(command, command)
        }
    }
})
