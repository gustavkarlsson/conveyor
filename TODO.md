* Consolidate AtomicStateFlow and StatefulMutableSharedFlow in respect to the new MutableStateFlow which supports atomic updates
* Fix website with links to api
* Mention api docs on readme
* Finish demo
* Finish vhs plugin
* Fix warning for `:core:detektJvmTest`
* Fix warning for `:core:detektJvmMain`
* Fix build warnings:
```
> Task :buildSrc:compileKotlin
w: Runtime JAR files in the classpath should have the same version. These files were found in the classpath:
    C:/Users/Gustav/.gradle/wrapper/dists/gradle-7.0-all/9m115ut5nwvtxli7nys8pggfr/gradle-7.0/lib/kotlin-stdlib-1.4.31.jar (version 1.4)
    C:/Users/Gustav/.gradle/wrapper/dists/gradle-7.0-all/9m115ut5nwvtxli7nys8pggfr/gradle-7.0/lib/kotlin-stdlib-common-1.4.31.jar (version 1.4)
    C:/Users/Gustav/.gradle/wrapper/dists/gradle-7.0-all/9m115ut5nwvtxli7nys8pggfr/gradle-7.0/lib/kotlin-stdlib-jdk7-1.4.31.jar (version 1.4)
    C:/Users/Gustav/.gradle/wrapper/dists/gradle-7.0-all/9m115ut5nwvtxli7nys8pggfr/gradle-7.0/lib/kotlin-stdlib-jdk8-1.4.31.jar (version 1.4)
    C:/Users/Gustav/.gradle/wrapper/dists/gradle-7.0-all/9m115ut5nwvtxli7nys8pggfr/gradle-7.0/lib/kotlin-reflect-1.4.31.jar (version 1.4)
    C:/Users/Gustav/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib-jdk8/1.4.31/e613be5465ef1e6fd0468707690b7ebf625ea2fe/kotlin-stdlib-jdk8-1.4.31.jar (version 1.4)
    C:/Users/Gustav/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-reflect/1.4.31/63db9d66c3d20f7b8f66196e7ba86969daae8b8a/kotlin-reflect-1.4.31.jar (version 1.4)
    C:/Users/Gustav/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib-jdk7/1.4.31/84ce8e85f6e84270b2b501d44e9f0ba6ff64fa71/kotlin-stdlib-jdk7-1.4.31.jar (version 1.4)
    C:/Users/Gustav/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib/1.5.0/29dae2501ca094416d15af0e21470cb634780444/kotlin-stdlib-1.5.0.jar (version 1.5)
    C:/Users/Gustav/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlin/kotlin-stdlib-common/1.5.0/4080d69efca5e39e9b4972f125e40f1607bd6460/kotlin-stdlib-common-1.5.0.jar (version 1.5)
w: Consider providing an explicit dependency on kotlin-reflect 1.5 to prevent strange errors
w: Some runtime JAR files in the classpath have an incompatible version. Consider removing them from the classpath
Configuration on demand is an incubating feature.
```
