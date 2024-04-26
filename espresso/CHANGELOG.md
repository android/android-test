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

* Fix slow inRoot operations in Robolectric
* Use PlatformTestStorageRegistry.getInstance consistently instead of passing a reference around
* Remove TODO from InteractionResponse public ref docs

**New Features**

* Add waitForClose to DrawerActions.

**Breaking Changes**

**API Changes**

* Adapt to ViewCapture API changes
* Delete ViewInteraction.captureToBitmap in favor of ViewActions.captureToBitmap


**Breaking API Changes**

**Known Issues**
