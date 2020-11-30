var pages = [{"name":"fun <State> Action(block: suspend (state: UpdatableStateFlow<State>) -> Unit): Action<State>","description":"se.gustavkarlsson.conveyor.Action","location":"core/se.gustavkarlsson.conveyor/-action.html","searchKeys":["Action","fun <State> Action(block: suspend (state: UpdatableStateFlow<State>) -> Unit): Action<State>"]},{"name":"abstract suspend fun execute(state: UpdatableStateFlow<State>)","description":"se.gustavkarlsson.conveyor.Action.execute","location":"core/se.gustavkarlsson.conveyor/-action/execute.html","searchKeys":["execute","abstract suspend fun execute(state: UpdatableStateFlow<State>)"]},{"name":"interface ActionIssuer<State>","description":"se.gustavkarlsson.conveyor.ActionIssuer","location":"core/se.gustavkarlsson.conveyor/-action-issuer/index.html","searchKeys":["ActionIssuer","interface ActionIssuer<State>"]},{"name":"abstract fun issue(action: Action<State>)","description":"se.gustavkarlsson.conveyor.ActionIssuer.issue","location":"core/se.gustavkarlsson.conveyor/-action-issuer/issue.html","searchKeys":["issue","abstract fun issue(action: Action<State>)"]},{"name":"fun <State> ActionIssuer<State>.issue(block: suspend (state: UpdatableStateFlow<State>) -> Unit)","description":"se.gustavkarlsson.conveyor.issue","location":"core/se.gustavkarlsson.conveyor/issue.html","searchKeys":["issue","fun <State> ActionIssuer<State>.issue(block: suspend (state: UpdatableStateFlow<State>) -> Unit)"]},{"name":"interface Plugin<State>","description":"se.gustavkarlsson.conveyor.Plugin","location":"core/se.gustavkarlsson.conveyor/-plugin/index.html","searchKeys":["Plugin","interface Plugin<State>"]},{"name":"open fun addStartActions(): Iterable<Action<State>>","description":"se.gustavkarlsson.conveyor.Plugin.addStartActions","location":"core/se.gustavkarlsson.conveyor/-plugin/add-start-actions.html","searchKeys":["addStartActions","open fun addStartActions(): Iterable<Action<State>>"]},{"name":"open fun overrideInitialState(initialState: State): State","description":"se.gustavkarlsson.conveyor.Plugin.overrideInitialState","location":"core/se.gustavkarlsson.conveyor/-plugin/override-initial-state.html","searchKeys":["overrideInitialState","open fun overrideInitialState(initialState: State): State"]},{"name":"open fun transformActions(actions: Flow<Action<State>>): Flow<Action<State>>","description":"se.gustavkarlsson.conveyor.Plugin.transformActions","location":"core/se.gustavkarlsson.conveyor/-plugin/transform-actions.html","searchKeys":["transformActions","open fun transformActions(actions: Flow<Action<State>>): Flow<Action<State>>"]},{"name":"open fun transformStates(states: Flow<State>): Flow<State>","description":"se.gustavkarlsson.conveyor.Plugin.transformStates","location":"core/se.gustavkarlsson.conveyor/-plugin/transform-states.html","searchKeys":["transformStates","open fun transformStates(states: Flow<State>): Flow<State>"]},{"name":"fun <State> Store(initialState: State, startActions: Iterable<Action<State>> = emptyList(), plugins: Iterable<Plugin<State>> = emptyList()): Store<State>","description":"se.gustavkarlsson.conveyor.Store","location":"core/se.gustavkarlsson.conveyor/-store.html","searchKeys":["Store","fun <State> Store(initialState: State, startActions: Iterable<Action<State>> = emptyList(), plugins: Iterable<Plugin<State>> = emptyList()): Store<State>"]},{"name":"abstract val job: Job?","description":"se.gustavkarlsson.conveyor.Store.job","location":"core/se.gustavkarlsson.conveyor/-store/job.html","searchKeys":["job","abstract val job: Job?"]},{"name":"abstract fun start(scope: CoroutineScope): Job","description":"se.gustavkarlsson.conveyor.Store.start","location":"core/se.gustavkarlsson.conveyor/-store/start.html","searchKeys":["start","abstract fun start(scope: CoroutineScope): Job"]},{"name":"fun <State> CoroutineScope.start(store: Store<State>): Job","description":"se.gustavkarlsson.conveyor.start","location":"core/se.gustavkarlsson.conveyor/start.html","searchKeys":["start","fun <State> CoroutineScope.start(store: Store<State>): Job"]},{"name":"abstract val state: StateFlow<State>","description":"se.gustavkarlsson.conveyor.Store.state","location":"core/se.gustavkarlsson.conveyor/-store/state.html","searchKeys":["state","abstract val state: StateFlow<State>"]},{"name":"class StoreAlreadyStartedException : IllegalStateException","description":"se.gustavkarlsson.conveyor.StoreAlreadyStartedException","location":"core/se.gustavkarlsson.conveyor/-store-already-started-exception/index.html","searchKeys":["StoreAlreadyStartedException","class StoreAlreadyStartedException : IllegalStateException"]},{"name":"fun StoreAlreadyStartedException()","description":"se.gustavkarlsson.conveyor.StoreAlreadyStartedException.StoreAlreadyStartedException","location":"core/se.gustavkarlsson.conveyor/-store-already-started-exception/-store-already-started-exception.html","searchKeys":["StoreAlreadyStartedException","fun StoreAlreadyStartedException()"]},{"name":"open val cause: Throwable?","description":"se.gustavkarlsson.conveyor.StoreAlreadyStartedException.cause","location":"core/se.gustavkarlsson.conveyor/-store-already-started-exception/cause.html","searchKeys":["cause","open val cause: Throwable?"]},{"name":"open val cause: Throwable?","description":"se.gustavkarlsson.conveyor.StoreNotYetStartedException.cause","location":"core/se.gustavkarlsson.conveyor/-store-not-yet-started-exception/cause.html","searchKeys":["cause","open val cause: Throwable?"]},{"name":"open val cause: Throwable?","description":"se.gustavkarlsson.conveyor.StoreStoppedException.cause","location":"core/se.gustavkarlsson.conveyor/-store-stopped-exception/cause.html","searchKeys":["cause","open val cause: Throwable?"]},{"name":"open val message: String?","description":"se.gustavkarlsson.conveyor.StoreAlreadyStartedException.message","location":"core/se.gustavkarlsson.conveyor/-store-already-started-exception/message.html","searchKeys":["message","open val message: String?"]},{"name":"open val message: String?","description":"se.gustavkarlsson.conveyor.StoreNotYetStartedException.message","location":"core/se.gustavkarlsson.conveyor/-store-not-yet-started-exception/message.html","searchKeys":["message","open val message: String?"]},{"name":"open val message: String?","description":"se.gustavkarlsson.conveyor.StoreStoppedException.message","location":"core/se.gustavkarlsson.conveyor/-store-stopped-exception/message.html","searchKeys":["message","open val message: String?"]},{"name":"class StoreNotYetStartedException : IllegalStateException","description":"se.gustavkarlsson.conveyor.StoreNotYetStartedException","location":"core/se.gustavkarlsson.conveyor/-store-not-yet-started-exception/index.html","searchKeys":["StoreNotYetStartedException","class StoreNotYetStartedException : IllegalStateException"]},{"name":"fun StoreNotYetStartedException()","description":"se.gustavkarlsson.conveyor.StoreNotYetStartedException.StoreNotYetStartedException","location":"core/se.gustavkarlsson.conveyor/-store-not-yet-started-exception/-store-not-yet-started-exception.html","searchKeys":["StoreNotYetStartedException","fun StoreNotYetStartedException()"]},{"name":"class StoreStoppedException(cancellationReason: Throwable?) : IllegalStateException","description":"se.gustavkarlsson.conveyor.StoreStoppedException","location":"core/se.gustavkarlsson.conveyor/-store-stopped-exception/index.html","searchKeys":["StoreStoppedException","class StoreStoppedException(cancellationReason: Throwable?) : IllegalStateException"]},{"name":"fun StoreStoppedException(cancellationReason: Throwable?)","description":"se.gustavkarlsson.conveyor.StoreStoppedException.StoreStoppedException","location":"core/se.gustavkarlsson.conveyor/-store-stopped-exception/-store-stopped-exception.html","searchKeys":["StoreStoppedException","fun StoreStoppedException(cancellationReason: Throwable?)"]},{"name":"val cancellationReason: Throwable?","description":"se.gustavkarlsson.conveyor.StoreStoppedException.cancellationReason","location":"core/se.gustavkarlsson.conveyor/-store-stopped-exception/cancellation-reason.html","searchKeys":["cancellationReason","val cancellationReason: Throwable?"]},{"name":"interface UpdatableStateFlow<State> : StateFlow<State> ","description":"se.gustavkarlsson.conveyor.UpdatableStateFlow","location":"core/se.gustavkarlsson.conveyor/-updatable-state-flow/index.html","searchKeys":["UpdatableStateFlow","interface UpdatableStateFlow<State> : StateFlow<State> "]},{"name":"abstract val replayCache: List<State>","description":"se.gustavkarlsson.conveyor.UpdatableStateFlow.replayCache","location":"core/se.gustavkarlsson.conveyor/-updatable-state-flow/replay-cache.html","searchKeys":["replayCache","abstract val replayCache: List<State>"]},{"name":"abstract suspend fun update(block: suspend State.() -> State): State","description":"se.gustavkarlsson.conveyor.UpdatableStateFlow.update","location":"core/se.gustavkarlsson.conveyor/-updatable-state-flow/update.html","searchKeys":["update","abstract suspend fun update(block: suspend State.() -> State): State"]},{"name":"abstract val value: State","description":"se.gustavkarlsson.conveyor.UpdatableStateFlow.value","location":"core/se.gustavkarlsson.conveyor/-updatable-state-flow/value.html","searchKeys":["value","abstract val value: State"]}]
