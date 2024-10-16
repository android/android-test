### services {version} {:#services-{version}}

{{date}}

`androidx.test.services:test-services:{version}` `androidx.test.services:storage:{version}` are released.

**Bug Fixes**

* TestStorage: Use input directory location for internal files 
* StackTrimmer: harden against exceptions coming from Failure.getMessage().

**New Features**

* LocalSocketProtocol: a replacement for SpeakEasy.
* ShellCommandLocalSocketClient: client that speaks LocalSocketProtocol.
* ShellCommandLocalSocketExecutorServer: server that speaks LocalSocketProtocol.
* LocalSocketShellMain: a replacement for ShellMain. Using this in place of
  ShellMain avoids the use of the SpeakEasy protocol, so androidx.test.services
  can be freely killed and restarted by the operating system without breaking
  tests. The ShellExecutorFactory will automatically figure out which protocol
  to use based on the binder key passed down by the variant of ShellMain.

**Breaking Changes**

**API Changes**

* Update to minSdkVersion 21

**Breaking API Changes**

**Known Issues**
