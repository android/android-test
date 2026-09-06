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

* Replace now-unnecessary reflection from TestLooperManagerCompat when using Android SDK 36 APIs
* Don't suppress AppNotIdleException if dumpThreadStates throws.
* Remove Espresso.onIdle tracing
* Fix NullPointerException in UiControllerImpl.

**New Features**

* Add `doesNotHaveFlag` and `doesNotHaveFlags` matchers to `IntentMatchers`.

**Breaking Changes**

**API Changes**

* Update to minSdkVersion 24 and remove all related logic for SDKs < 24

**Breaking API Changes**

**Known Issues**

**New Locator declared**
