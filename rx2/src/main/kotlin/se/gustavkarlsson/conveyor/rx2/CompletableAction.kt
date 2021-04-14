package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Completable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.rx2.await
import se.gustavkarlsson.conveyor.Action
import se.gustavkarlsson.conveyor.UpdatableStateFlow
import se.gustavkarlsson.conveyor.rx2.internal.UpdatableStateFlowableImpl

/**
 * An [Action] that executes using a [Completable].
 */
@ExperimentalCoroutinesApi
public abstract class CompletableAction<State : Any> : Action<State> {
    final override suspend fun execute(stateFlow: UpdatableStateFlow<State>) {
        val flowable = UpdatableStateFlowableImpl(stateFlow)
        val completable = execute(flowable)
        completable.await()
    }

    /**
     * The completable that runs the action. The state can be accessed through the state argument.
     */
    protected abstract fun execute(stateFlow: UpdatableStateFlowable<State>): Completable
}

/**
 * Creates an action with the completable returned from the given block as its execute function.
 */
@ExperimentalCoroutinesApi
@Suppress("FunctionName")
public fun <State : Any> CompletableAction(
    block: (state: UpdatableStateFlowable<State>) -> Completable,
): CompletableAction<State> = ConstructorCompletableAction(block)

@ExperimentalCoroutinesApi
private class ConstructorCompletableAction<State : Any>(
    private val block: (UpdatableStateFlowable<State>) -> Completable,
) : CompletableAction<State>() {
    override fun execute(stateFlow: UpdatableStateFlowable<State>): Completable = block(stateFlow)
}
