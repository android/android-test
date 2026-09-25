### runner {version} {:#runner-{version}}

{{date}}

`androidx.test:runner:{version}` is released.

**Bug Fixes**

* Catch LinkageError when loading annotation classes (b/536117227)

* Support filtering parameterized tests with arbitrary (non-numeric) parameter names by their root method name. (b/564761639)

**New Features**

* Make perfetto trace sections for tests more identifiable by prefixing with "test:" and using fully qualified class name. (b/204992764)

* Add logs at the start and end of RunBefore and RunAfters sections to help bug understanding. (b/445754263)

**Breaking Changes**

**API Changes**

* Update to minSdkVersion 24 and remove all related logic for SDKs < 24

**Breaking API Changes**

**Known Issues**
