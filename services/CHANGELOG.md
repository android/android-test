### services 1.5.0-alpha02 {:#services-1.5.0-alpha02}

{{date}}

`androidx.test.services:test-services:1.5.0-alpha02` `androidx.test.services:storage:1.5.0-alpha02` are released.

**API Changes**

* Adding `ShellExecutorFactory` and `ShellExecutorFileObserverImpl`. `ShellExecutorImpl`'s constructor is no longer public
* Adding `CoroutineFileObserver`, `FileObserverProtocol`, `ShellCommandFileObserverClient`, and `FileObserverShellMain`

**Bug Fixes**

* Attempt to avoid outputting a test result summary which exceeds binder transaction limit

**Dependency Changes/Updates**

* minSdkVersion is now 19, targetSdkVersion is now 34

**Other**

* Fix maven apk source collection.
