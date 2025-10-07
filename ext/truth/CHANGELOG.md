### ext.truth {version} {:#ext.truth-{version}}

{{date}}

`androidx.test.ext:truth:{version}` is released.

**Bug Fixes**

**New Features**

* `BundleSubject` and `PersistableBundleSubject` now share `BaseBundle` support,
  including additional array type assertions
* `PersistableBundleSubject` now supports `isEqualTo` and `isNotEqualTo` for
  direct comparison between two `PersistableBundle`'s.
* Added `integerArrayList` method to `BundleSubject`.

**Breaking Changes**

**API Changes**

* Update to minSdkVersion 23 and remove all related logic for SDKs < 23

**Breaking API Changes**

**Known Issues**
