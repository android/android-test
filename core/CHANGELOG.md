### Core Core-ktx {version} {:#core-{version}}

{{date}}

`androidx.test:core:{version}` and `androidx.test:core-ktx:{version}` are released.

**Bug Fixes**

* Fixes the issue where the system caches the extras from the previous intent for the PendingIntent and thus the extras passed to the PendingIntent for the new Intent would be ignored.

**New Features**

**Breaking Changes**

**API Changes**

* Update to minSdkVersion 23 and remove all related logic for SDKs < 23

**Breaking API Changes**

**Known Issues**
