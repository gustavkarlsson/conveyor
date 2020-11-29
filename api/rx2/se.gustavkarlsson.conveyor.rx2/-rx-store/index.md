---
title: RxStore -
---
//[rx2](../../index.md)/[se.gustavkarlsson.conveyor.rx2](../index.md)/[RxStore](index.md)



# RxStore  
 [jvm] interface [RxStore](index.md)<[State](index.md) : [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)> : ActionIssuer<[State](index.md)>    


## Functions  
  
|  Name|  Summary| 
|---|---|
| <a name="kotlin/Any/equals/#kotlin.Any?/PointingToDeclaration/"></a>[equals](../-updatable-state-flowable/index.md#%5Bkotlin%2FAny%2Fequals%2F%23kotlin.Any%3F%2FPointingToDeclaration%2F%5D%2FFunctions%2F-445135835)| <a name="kotlin/Any/equals/#kotlin.Any?/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>open operator fun [equals](../-updatable-state-flowable/index.md#%5Bkotlin%2FAny%2Fequals%2F%23kotlin.Any%3F%2FPointingToDeclaration%2F%5D%2FFunctions%2F-445135835)(other: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)  <br><br><br>
| <a name="kotlin/Any/hashCode/#/PointingToDeclaration/"></a>[hashCode](../-updatable-state-flowable/index.md#%5Bkotlin%2FAny%2FhashCode%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F-445135835)| <a name="kotlin/Any/hashCode/#/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>open fun [hashCode](../-updatable-state-flowable/index.md#%5Bkotlin%2FAny%2FhashCode%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F-445135835)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)  <br><br><br>
| <a name="se.gustavkarlsson.conveyor/ActionIssuer/issue/#se.gustavkarlsson.conveyor.Action[TypeParam(bounds=[kotlin.Any])]/PointingToDeclaration/"></a>[issue](index.md#%5Bse.gustavkarlsson.conveyor%2FActionIssuer%2Fissue%2F%23se.gustavkarlsson.conveyor.Action%5BTypeParam%28bounds%3D%5Bkotlin.Any%5D%29%5D%2FPointingToDeclaration%2F%5D%2FFunctions%2F-445135835)| <a name="se.gustavkarlsson.conveyor/ActionIssuer/issue/#se.gustavkarlsson.conveyor.Action[TypeParam(bounds=[kotlin.Any])]/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>abstract fun [issue](index.md#%5Bse.gustavkarlsson.conveyor%2FActionIssuer%2Fissue%2F%23se.gustavkarlsson.conveyor.Action%5BTypeParam%28bounds%3D%5Bkotlin.Any%5D%29%5D%2FPointingToDeclaration%2F%5D%2FFunctions%2F-445135835)(action: Action<[State](index.md)>)  <br><br><br>
| <a name="se.gustavkarlsson.conveyor.rx2/RxStore/start/#kotlinx.coroutines.CoroutineScope/PointingToDeclaration/"></a>[start](start.md)| <a name="se.gustavkarlsson.conveyor.rx2/RxStore/start/#kotlinx.coroutines.CoroutineScope/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>abstract fun [start](start.md)(scope: CoroutineScope = GlobalScope): Disposable  <br><br><br>
| <a name="kotlin/Any/toString/#/PointingToDeclaration/"></a>[toString](../-updatable-state-flowable/index.md#%5Bkotlin%2FAny%2FtoString%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F-445135835)| <a name="kotlin/Any/toString/#/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>open fun [toString](../-updatable-state-flowable/index.md#%5Bkotlin%2FAny%2FtoString%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F-445135835)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)  <br><br><br>


## Properties  
  
|  Name|  Summary| 
|---|---|
| <a name="se.gustavkarlsson.conveyor.rx2/RxStore/state/#/PointingToDeclaration/"></a>[state](state.md)| <a name="se.gustavkarlsson.conveyor.rx2/RxStore/state/#/PointingToDeclaration/"></a> [jvm] abstract val [state](state.md): [StateFlowable](../-state-flowable/index.md)<[State](index.md)>   <br>

