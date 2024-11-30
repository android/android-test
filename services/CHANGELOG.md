### services {version} {:#services-{version}}

{{date}}

`androidx.test.services:test-services:{version}` `androidx.test.services:storage:{version}` are released.

**Bug Fixes**

**New Features**

* Adding a LocalSocket-based protocol for the ShellExecutor to talk to the
  ShellMain. This obsoletes SpeakEasy; if androidx.test.services is killed
  (e.g. by the low memory killer) between the start of the app_process that
  invokes LocalSocketShellMain and the start of the test, the test is still able
  to talk to LocalSocketShellMain.

**Breaking Changes**

**API Changes**

**Breaking API Changes**

**Known Issues**
