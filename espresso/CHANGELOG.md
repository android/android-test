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

* Avoid enqueuing redundant idle notifications in `IdlingResourceRegistry`
  when an already-idle `IdlingResource` calls `onTransitionToIdle()`.
* Fix a race where Espresso could report the app as idle while the main
  thread still had a task queued by background work that had just gone idle,
  while preserving idling policy timeout enforcement across re-checks.

**New Features**

**Breaking Changes**

**API Changes**

**Breaking API Changes**

**Known Issues**
