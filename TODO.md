* Race condition during tests (using rxjava only?). Runs on coroutine thread instead... GlobalScope?
* UpdatableStateFlowable.update should have a non-rx overload
* Let buildStore() return something that does not allow issuing actions, and let store.start() return one instead. Maybe it should implement job? ActionIssuer
* Add AbstractLiveAction?
* Add WatchAction?
* Add watchers?
* Add interceptors?
* Add plugin support?
