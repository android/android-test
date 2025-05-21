### rules {version} {:#rules-{version}}

{{date}}

`androidx.test:rules:{version}` is released.

**Bug Fixes**

**New Features**

**Breaking Changes**

* `ServiceTestRule.startService` will now throw if the provided intent does not
  launch a service.

**API Changes**

* `GrantPermissionRule` now has a method `#grantImmediately` to grant permissions without going through the `TestRule` infrastructure.

**Breaking API Changes**

**Known Issues**
