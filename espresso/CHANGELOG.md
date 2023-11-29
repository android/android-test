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

* Fix the description of IsPlatformPopup to match the behavior.
* Fix deprecated obtainMovement impl that used the wrong coordinates.
* Replace broken links to junit.org javadoc with @link.

**API Changes**

* Adding a new IsActivatedMatcher to verify if it is activated or not.
* Makes Espresso.onIdle() work on the main thread to allow for draining the main thread from the main thread.
* minSdkVersion is now 19, targetSdkVersion is now 34
* Add scrollTo variant that allows scrolling to 90+% displayed views
