### services {version} {:#services-{version}}

{{date}}

<!-- disableFinding(LINE_OVER_80) -->
`androidx.test.services:test-services:{version}` `androidx.test.services:storage:{version}` are released.

**Bug Fixes**

* Ensure TestStorage library is multi-OS-user compatible.

**New Features**

**Breaking Changes**

* The location where TestStorage stores some files on-device has changed for
APIs 29+. This is non-breaking if using the TestStorage API or using
ContentProviders, but it is breaking if tests or frameworks depended on the
explicit location of the output files.

**API Changes**

* Update to minSdkVersion 23 and remove all related logic for SDKs < 23

**Breaking API Changes**

**Known Issues**
