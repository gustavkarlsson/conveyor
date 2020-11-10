package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Single
import io.reactivex.disposables.Disposable
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import se.gustavkarlsson.conveyor.buildStore
import se.gustavkarlsson.conveyor.rx2.test.memoizedTestCoroutineScope

object RxStoreIntegrationTest : Spek({
    val scope by memoizedTestCoroutineScope()

    describe("A started RxStore") {
        val subject by memoized { buildStore(0).toRxStore() }

        var disposable: Disposable? = null
        beforeEachTest { disposable = subject.start(scope) }
        afterEachTest { disposable?.dispose() }

        it("Executes sequentially") {
            val testSubscriber = subject.state.test()
            subject.issue { state ->
                state.update { Single.just(1) }.ignoreElement()
            }
            testSubscriber.assertValues(0, 1)
        }
    }
})
