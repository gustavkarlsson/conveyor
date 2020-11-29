---
title: se.gustavkarlsson.conveyor.rx2 -
---
//[rx2](../index.md)/[se.gustavkarlsson.conveyor.rx2](index.md)



# Package se.gustavkarlsson.conveyor.rx2  


## Types  
  
|  Name|  Summary| 
|---|---|
| <a name="se.gustavkarlsson.conveyor.rx2/CompletableAction///PointingToDeclaration/"></a>[CompletableAction](-completable-action/index.md)| <a name="se.gustavkarlsson.conveyor.rx2/CompletableAction///PointingToDeclaration/"></a>[jvm]  <br>Content  <br>@ExperimentalCoroutinesApi()  <br>  <br>abstract class [CompletableAction](-completable-action/index.md)<[State](-completable-action/index.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)> : Action<[State](-completable-action/index.md)>   <br><br><br>
| <a name="se.gustavkarlsson.conveyor.rx2/RxStore///PointingToDeclaration/"></a>[RxStore](-rx-store/index.md)| <a name="se.gustavkarlsson.conveyor.rx2/RxStore///PointingToDeclaration/"></a>[jvm]  <br>Content  <br>interface [RxStore](-rx-store/index.md)<[State](-rx-store/index.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)> : ActionIssuer<[State](-rx-store/index.md)>   <br><br><br>
| <a name="se.gustavkarlsson.conveyor.rx2/StateFlowable///PointingToDeclaration/"></a>[StateFlowable](-state-flowable/index.md)| <a name="se.gustavkarlsson.conveyor.rx2/StateFlowable///PointingToDeclaration/"></a>[jvm]  <br>Content  <br>abstract class [StateFlowable](-state-flowable/index.md)<[State](-state-flowable/index.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)> : Flowable<[State](-state-flowable/index.md)>   <br><br><br>
| <a name="se.gustavkarlsson.conveyor.rx2/UpdatableStateFlowable///PointingToDeclaration/"></a>[UpdatableStateFlowable](-updatable-state-flowable/index.md)| <a name="se.gustavkarlsson.conveyor.rx2/UpdatableStateFlowable///PointingToDeclaration/"></a>[jvm]  <br>Content  <br>abstract class [UpdatableStateFlowable](-updatable-state-flowable/index.md)<[State](-updatable-state-flowable/index.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)> : [StateFlowable](-state-flowable/index.md)<[State](-updatable-state-flowable/index.md)>   <br><br><br>


## Functions  
  
|  Name|  Summary| 
|---|---|
| <a name="se.gustavkarlsson.conveyor.rx2//asRxStore/se.gustavkarlsson.conveyor.Store[TypeParam(bounds=[kotlin.Any])]#/PointingToDeclaration/"></a>[asRxStore](as-rx-store.md)| <a name="se.gustavkarlsson.conveyor.rx2//asRxStore/se.gustavkarlsson.conveyor.Store[TypeParam(bounds=[kotlin.Any])]#/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>@ExperimentalCoroutinesApi()  <br>  <br>fun <[State](as-rx-store.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)> Store<[State](as-rx-store.md)>.[asRxStore](as-rx-store.md)(): [RxStore](-rx-store/index.md)<[State](as-rx-store.md)>  <br><br><br>
| <a name="se.gustavkarlsson.conveyor.rx2//CompletableAction/#kotlin.Function1[se.gustavkarlsson.conveyor.rx2.UpdatableStateFlowable[TypeParam(bounds=[kotlin.Any])],io.reactivex.Completable]/PointingToDeclaration/"></a>[CompletableAction](-completable-action.md)| <a name="se.gustavkarlsson.conveyor.rx2//CompletableAction/#kotlin.Function1[se.gustavkarlsson.conveyor.rx2.UpdatableStateFlowable[TypeParam(bounds=[kotlin.Any])],io.reactivex.Completable]/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>@ExperimentalCoroutinesApi()  <br>  <br>fun <[State](-completable-action.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)> [CompletableAction](-completable-action.md)(block: (state: [UpdatableStateFlowable](-updatable-state-flowable/index.md)<[State](-completable-action.md)>) -> Completable): [CompletableAction](-completable-action/index.md)<[State](-completable-action.md)>  <br><br><br>
| <a name="se.gustavkarlsson.conveyor.rx2//issue/se.gustavkarlsson.conveyor.ActionIssuer[TypeParam(bounds=[kotlin.Any])]#kotlin.Function1[se.gustavkarlsson.conveyor.rx2.UpdatableStateFlowable[TypeParam(bounds=[kotlin.Any])],io.reactivex.Completable]/PointingToDeclaration/"></a>[issue](issue.md)| <a name="se.gustavkarlsson.conveyor.rx2//issue/se.gustavkarlsson.conveyor.ActionIssuer[TypeParam(bounds=[kotlin.Any])]#kotlin.Function1[se.gustavkarlsson.conveyor.rx2.UpdatableStateFlowable[TypeParam(bounds=[kotlin.Any])],io.reactivex.Completable]/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>@ExperimentalCoroutinesApi()  <br>  <br>fun <[State](issue.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)> ActionIssuer<[State](issue.md)>.[issue](issue.md)(block: (state: [UpdatableStateFlowable](-updatable-state-flowable/index.md)<[State](issue.md)>) -> Completable)  <br><br><br>

