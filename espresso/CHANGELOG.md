### Espresso 3.6.0-alpha02 {:#espresso-3.6.0-alpha02}

{{date}}

The following artifacts were released:

* `androidx.test.espresso:espresso-accessibility:3.6.0-alpha02`
* `androidx.test.espresso:espresso-core:3.6.0-alpha02`
* `androidx.test.espresso:espresso-contrib:3.6.0-alpha02`
* `androidx.test.espresso:espresso-idling-resource:3.6.0-alpha02`
* `androidx.test.espresso:espresso-intents:3.6.0-alpha02`
* `androidx.test.espresso:espresso-remote:3.6.0-alpha02`
* `androidx.test.espresso:espresso-web:3.6.0-alpha02`
* `androidx.test.espresso.idling:idling-concurrent:3.6.0-alpha02`
* `androidx.test.espresso.idling:idling-net:3.6.0-alpha02`

**API Changes**

* Move espresso/core protos to espresso/remote. The protos ship as part of espresso/remote, thus should be in that folder.
* Introduce @hide to properly restrict implementation details from public API
* Adding a new IsActivatedMatcher to verify if it is activated or not.
* ViewInteractionCapture removed from public API
* Makes onIdle() work on the main thread to allow for draining the main thread from the main thread.

**Bug Fixes**

* fork a common DirectExecutor to fix IllegalAccessError androidx.concurrent.DirectExecutor issues
* Fix the description of IsPlatformPopup to match the behavior.
* Fix deprecated obtainMovement impl that used the wrong coordinates.

**Dependency Changes/Updates**

* minSdkVersion is now 19, targetSdkVersion is now 34

**Documentation Updates**

* Replace broken links to junit.org javadoc with @link.
