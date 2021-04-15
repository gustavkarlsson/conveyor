package se.gustavkarlsson.conveyor.rx2

import io.reactivex.Single
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * A [StateFlowable] that can be updated sequentially, guaranteeing predictable state changes.
 */
public abstract class UpdatableStateFlowable<State : Any> : StateFlowable<State>() {
    /**
     * Updates the state using the given block, returning the new state in a single.
     * The receiver argument of the block is the current state at the time the block runs.
     *
     * State updates run sequentially, which guarantees that the state does not change while an update block runs.
     */
    public abstract fun update(block: State.() -> State): Single<State>

    /**
     * Updates the state using the given block, returning the new state.
     * The receiver argument of the block is the current state at the time the block runs.
     *
     * State updates run sequentially, which guarantees that the state does not change while an update block runs.
     * This function will therefore block if another update is in progress,
     * even if the provided block does not block.
     */
    public abstract fun updateBlocking(block: State.() -> State): State

    /**
     * The number of subscriber of the **store**
     * Acts like [MutableSharedFlow.subscriptionCount] but for the external state flowable of the store.
     */
    public abstract val storeSubscriberCount: StateFlowable<Int>
}
