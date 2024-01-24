### Espresso {version} {:#espresso-{version}}

{{date}}

The following artifacts were released:

* `androidx.test.espresso:espresso-accessibility:{version}`
* `androidx.test.espresso:espresso-core:{version}`
* `androidx.test.espresso:espresso-contrib:{version}`
* `androidx.test.espresso:espresso-idling-resource:{version}`
* `androidx.test.espresso:espresso-intents:{version}`
* `androidx.test.espresso:espresso-remote:{version}`
* `androidx.test.espresso:espresso-web:{version}`
* `androidx.test.espresso.idling:idling-concurrent:{version}`
* `androidx.test.espresso.idling:idling-net:{version}`

**Bug Fixes**

* Fix typo in AdapterDataLoaderAction error message
* Remove Kotlin collect stdlib calls in Java from espresso
* Reference doc cleanup - document previously missing parameters, fix links, etc
* Remove Kotlin StringKt calls from Java code
* Remove all support for Android SDKs < 19. Minimum is API 19 (Android Kit Kat 4.4)
* Stop posting empty tasks to background threads when running in non-remote mode
* Better handle exceptions that may occur in DefaultFailureHandler's hierarchy capture and screenshot process.

**New Features**

**Breaking Changes**

**API Changes**

* Mark generated IInteractionExecutionStatus class as RestrictTo LIBRARY_GROUP
* Remove ExperimentalTestApi from RuntimePermissionStubber

**Breaking API Changes**

**Known Issues**
