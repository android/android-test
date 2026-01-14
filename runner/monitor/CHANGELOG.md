### monitor {version} {:#monitor-{version}}

{{date}}

`androidx.test:monitor:{version}` is released.

**Bug Fixes**

* Fixes missing IntentMonitor for startActivity with user overload. Now it's
  possible to intercept intents started with startActivityAsUser.

* Adds missing override of an existing callActivityOnCreate method with
  PersistableBundle.

**New Features**

**Breaking Changes**

**API Changes**

* Update to minSdkVersion 23 and remove all related logic for SDKs < 23

**Breaking API Changes**

**Known Issues**
