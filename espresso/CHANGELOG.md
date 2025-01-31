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

* Fix deadlock in espresso in Robolectric INSTRUMENTATION_TEST + paused looper.
* Refactor espresso's MessageQueue access into a TestLooperManagerCompat class,
  and use new TestLooperManager APIs when available.

**New Features**

**Breaking Changes**

**API Changes**

* Update to minSdkVersion 21

**Breaking API Changes**

**Known Issues**
