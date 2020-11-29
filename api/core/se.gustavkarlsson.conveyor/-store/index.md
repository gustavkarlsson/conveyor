---
title: Store -
---
//[core](../../index.md)/[se.gustavkarlsson.conveyor](../index.md)/[Store](index.md)



# Store  
 [jvm] interface [Store](index.md)<[State](index.md)> : [ActionIssuer](../-action-issuer/index.md)<[State](index.md)>    


## Functions  
  
|  Name|  Summary| 
|---|---|
| <a name="kotlin/Any/equals/#kotlin.Any?/PointingToDeclaration/"></a>[equals](../-updatable-state-flow/index.md#%5Bkotlin%2FAny%2Fequals%2F%23kotlin.Any%3F%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)| <a name="kotlin/Any/equals/#kotlin.Any?/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>open operator fun [equals](../-updatable-state-flow/index.md#%5Bkotlin%2FAny%2Fequals%2F%23kotlin.Any%3F%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)(other: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)  <br><br><br>
| <a name="kotlin/Any/hashCode/#/PointingToDeclaration/"></a>[hashCode](../-updatable-state-flow/index.md#%5Bkotlin%2FAny%2FhashCode%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)| <a name="kotlin/Any/hashCode/#/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>open fun [hashCode](../-updatable-state-flow/index.md#%5Bkotlin%2FAny%2FhashCode%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)  <br><br><br>
| <a name="se.gustavkarlsson.conveyor/ActionIssuer/issue/#se.gustavkarlsson.conveyor.Action[TypeParam(bounds=[kotlin.Any?])]/PointingToDeclaration/"></a>[issue](../-action-issuer/issue.md)| <a name="se.gustavkarlsson.conveyor/ActionIssuer/issue/#se.gustavkarlsson.conveyor.Action[TypeParam(bounds=[kotlin.Any?])]/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>abstract fun [issue](../-action-issuer/issue.md)(action: [Action](../-action/index.md)<[State](index.md)>)  <br><br><br>
| <a name="se.gustavkarlsson.conveyor/Store/start/#kotlinx.coroutines.CoroutineScope/PointingToDeclaration/"></a>[start](start.md)| <a name="se.gustavkarlsson.conveyor/Store/start/#kotlinx.coroutines.CoroutineScope/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>abstract fun [start](start.md)(scope: CoroutineScope): Job  <br><br><br>
| <a name="kotlin/Any/toString/#/PointingToDeclaration/"></a>[toString](../-updatable-state-flow/index.md#%5Bkotlin%2FAny%2FtoString%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)| <a name="kotlin/Any/toString/#/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>open fun [toString](../-updatable-state-flow/index.md#%5Bkotlin%2FAny%2FtoString%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)  <br><br><br>


## Properties  
  
|  Name|  Summary| 
|---|---|
| <a name="se.gustavkarlsson.conveyor/Store/state/#/PointingToDeclaration/"></a>[state](state.md)| <a name="se.gustavkarlsson.conveyor/Store/state/#/PointingToDeclaration/"></a> [jvm] abstract val [state](state.md): StateFlow<[State](index.md)>   <br>

