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

**Bug Fixes**

* Fix the description of IsPlatformPopup to match the behavior.
* Fix deprecated obtainMovement impl that used the wrong coordinates.
* Replace broken links to junit.org javadoc with @link.

**API Changes**

* Adding a new IsActivatedMatcher to verify if it is activated or not.
* Makes Espresso.onIdle() work on the main thread to allow for draining the main thread from the main thread.
* minSdkVersion is now 19, targetSdkVersion is now 34
