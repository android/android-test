### services {version} {:#services-{version}}

{{date}}

`androidx.test.services:test-services:{version}` `androidx.test.services:storage:{version}` are released.

**Bug Fixes**

* TestStorage: Use input directory location for internal files 
* StackTrimmer: harden against exceptions coming from Failure.getMessage().

**New Features**

* Adding a LocalSocket-based protocol for the ShellExecutor to talk to the
  ShellMain. This obsoletes SpeakEasy; if androidx.test.services is killed
  (e.g. by the low memory killer) between the start of the app_process that
  invokes LocalSocketShellMain and the start of the test, the test is still able
  to talk to LocalSocketShellMain.

**Breaking Changes**

**API Changes**

* Update to minSdkVersion 21

**Breaking API Changes**

**Known Issues**
