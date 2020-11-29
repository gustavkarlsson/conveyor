---
title: UpdatableStateFlow -
---
//[core](../../index.md)/[se.gustavkarlsson.conveyor](../index.md)/[UpdatableStateFlow](index.md)



# UpdatableStateFlow  
 [jvm] interface [UpdatableStateFlow](index.md)<[State](index.md)> : StateFlow<[State](index.md)>    


## Functions  
  
|  Name|  Summary| 
|---|---|
| <a name="kotlinx.coroutines.flow/Flow/collect/#kotlinx.coroutines.flow.FlowCollector[TypeParam(bounds=[kotlin.Any?])]/PointingToDeclaration/"></a>[collect](index.md#%5Bkotlinx.coroutines.flow%2FFlow%2Fcollect%2F%23kotlinx.coroutines.flow.FlowCollector%5BTypeParam%28bounds%3D%5Bkotlin.Any%3F%5D%29%5D%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)| <a name="kotlinx.coroutines.flow/Flow/collect/#kotlinx.coroutines.flow.FlowCollector[TypeParam(bounds=[kotlin.Any?])]/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>abstract suspend fun [collect](index.md#%5Bkotlinx.coroutines.flow%2FFlow%2Fcollect%2F%23kotlinx.coroutines.flow.FlowCollector%5BTypeParam%28bounds%3D%5Bkotlin.Any%3F%5D%29%5D%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)(collector: FlowCollector<[State](index.md)>)  <br><br><br>
| <a name="kotlin/Any/equals/#kotlin.Any?/PointingToDeclaration/"></a>[equals](index.md#%5Bkotlin%2FAny%2Fequals%2F%23kotlin.Any%3F%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)| <a name="kotlin/Any/equals/#kotlin.Any?/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>open operator fun [equals](index.md#%5Bkotlin%2FAny%2Fequals%2F%23kotlin.Any%3F%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)(other: [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)?): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)  <br><br><br>
| <a name="kotlin/Any/hashCode/#/PointingToDeclaration/"></a>[hashCode](index.md#%5Bkotlin%2FAny%2FhashCode%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)| <a name="kotlin/Any/hashCode/#/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>open fun [hashCode](index.md#%5Bkotlin%2FAny%2FhashCode%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)(): [Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)  <br><br><br>
| <a name="kotlin/Any/toString/#/PointingToDeclaration/"></a>[toString](index.md#%5Bkotlin%2FAny%2FtoString%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)| <a name="kotlin/Any/toString/#/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>open fun [toString](index.md#%5Bkotlin%2FAny%2FtoString%2F%23%2FPointingToDeclaration%2F%5D%2FFunctions%2F223044804)(): [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)  <br><br><br>
| <a name="se.gustavkarlsson.conveyor/UpdatableStateFlow/update/#kotlin.coroutines.SuspendFunction1[TypeParam(bounds=[kotlin.Any?]),TypeParam(bounds=[kotlin.Any?])]/PointingToDeclaration/"></a>[update](update.md)| <a name="se.gustavkarlsson.conveyor/UpdatableStateFlow/update/#kotlin.coroutines.SuspendFunction1[TypeParam(bounds=[kotlin.Any?]),TypeParam(bounds=[kotlin.Any?])]/PointingToDeclaration/"></a>[jvm]  <br>Content  <br>abstract suspend fun [update](update.md)(block: suspend [State](index.md).() -> [State](index.md)): [State](index.md)  <br><br><br>


## Properties  
  
|  Name|  Summary| 
|---|---|
| <a name="se.gustavkarlsson.conveyor/UpdatableStateFlow/replayCache/#/PointingToDeclaration/"></a>[replayCache](replay-cache.md)| <a name="se.gustavkarlsson.conveyor/UpdatableStateFlow/replayCache/#/PointingToDeclaration/"></a> [jvm] abstract val [replayCache](replay-cache.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<[State](index.md)>   <br>
| <a name="se.gustavkarlsson.conveyor/UpdatableStateFlow/subscriptionCount/#/PointingToDeclaration/"></a>[subscriptionCount](subscription-count.md)| <a name="se.gustavkarlsson.conveyor/UpdatableStateFlow/subscriptionCount/#/PointingToDeclaration/"></a> [jvm] abstract val [subscriptionCount](subscription-count.md): StateFlow<[Int](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)>   <br>
| <a name="se.gustavkarlsson.conveyor/UpdatableStateFlow/value/#/PointingToDeclaration/"></a>[value](value.md)| <a name="se.gustavkarlsson.conveyor/UpdatableStateFlow/value/#/PointingToDeclaration/"></a> [jvm] abstract val [value](value.md): [State](index.md)   <br>

