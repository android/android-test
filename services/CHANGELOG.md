### services {version} {:#services-{version}}

{{date}}

`androidx.test.services:test-services:{version}` `androidx.test.services:storage:{version}` are released.

**Bug Fixes**

* TestStorage: use local cache dir to store output files when running as non system user

**New Features**

**Breaking Changes**

**API Changes**

* Upstream TestStorage.isTestStoragePath to PlatformTestStorage
* Upstream TestStorage.getInputFileUri and getOutputFileUri  to PlatformTestStorage
* Change openInternal* methods to throw FileNotFoundException instead of 
  IOException for consistency

**Breaking API Changes**

**Known Issues**
