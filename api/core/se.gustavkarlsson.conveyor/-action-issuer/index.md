---
title: ActionIssuer -
---
//[core](../../index.md)/[se.gustavkarlsson.conveyor](../index.md)/[ActionIssuer](index.md)



# ActionIssuer  
 [jvm] interface [ActionIssuer](index.md)<[State](index.md)>   


## Functions  
  
|  Name|  Summary| 
|---|---|
| <a name="kotlin/Any/equals/#kotlin.Any?/PointingToDeclaration/"></a>[equals](../-updatable-state-flow/index.md#%5Bkotlin%2FAny%2Fequals%2F%23kotlin.Any%3F%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)| <a name="kotlin/Any/equals/#kotlin.Any?/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>open operator fun [equals](../-updatable-state-flow/index.md#%5Bkotlin%2FAny%2Fequals%2F%23kotlin.Any%3F%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)(other: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)  <br><br><br>
| <a name="kotlin/Any/hashCode/#/PointingToDeclaration/"></a>[hashCode](../-updatable-state-flow/index.md#%5Bkotlin%2FAny%2FhashCode%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)| <a name="kotlin/Any/hashCode/#/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>open fun [hashCode](../-updatable-state-flow/index.md#%5Bkotlin%2FAny%2FhashCode%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)  <br><br><br>
| <a name="se.gustavkarlsson.conveyor/ActionIssuer/issue/#se.gustavkarlsson.conveyor.Action[TypeParam(bounds=[kotlin.Any?])]/PointingToDeclaration/"></a>[issue](issue.md)| <a name="se.gustavkarlsson.conveyor/ActionIssuer/issue/#se.gustavkarlsson.conveyor.Action[TypeParam(bounds=[kotlin.Any?])]/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>abstract fun [issue](issue.md)(action: [Action](../-action/index.md)<[State](index.md)>)  <br><br><br>
| <a name="kotlin/Any/toString/#/PointingToDeclaration/"></a>[toString](../-updatable-state-flow/index.md#%5Bkotlin%2FAny%2FtoString%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)| <a name="kotlin/Any/toString/#/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>open fun [toString](../-updatable-state-flow/index.md#%5Bkotlin%2FAny%2FtoString%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)  <br><br><br>


## Inheritors  
  
|  Name| 
|---|
| <a name="se.gustavkarlsson.conveyor/Store///PointingToDeclaration/"></a>[Store](../-store/index.md)


## Extensions  
  
|  Name|  Summary| 
|---|---|
| <a name="se.gustavkarlsson.conveyor//issue/se.gustavkarlsson.conveyor.ActionIssuer[TypeParam(bounds=[kotlin.Any?])]#kotlin.coroutines.SuspendFunction1[se.gustavkarlsson.conveyor.UpdatableStateFlow[TypeParam(bounds=[kotlin.Any?])],kotlin.Unit]/PointingToDeclaration/"></a>[issue](../issue.md)| <a name="se.gustavkarlsson.conveyor//issue/se.gustavkarlsson.conveyor.ActionIssuer[TypeParam(bounds=[kotlin.Any?])]#kotlin.coroutines.SuspendFunction1[se.gustavkarlsson.conveyor.UpdatableStateFlow[TypeParam(bounds=[kotlin.Any?])],kotlin.Unit]/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>fun <[State](../issue.md)> [ActionIssuer](index.md)<[State](../issue.md)>.[issue](../issue.md)(block: suspend (state: [UpdatableStateFlow](../-updatable-state-flow/index.md)<[State](../issue.md)>) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))  <br><br><br>

