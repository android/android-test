### runner 1.6.0-alpha05 {:#runner-1.6.0-alpha05}

{{date}}

`androidx.test:runner:1.6.0-alpha05` is released.

**Bug Fixes**

* `TestDiscoveryEventServiceConnection.send()` will correctly fail the test instead of hanging if the instrumentation throws a RuntimeException.
* Stop reparsing all args for every AndroidJUnit4 test class. This should address initialization errors like in #1948.

**API Changes**

* minSdkVersion is now 19
