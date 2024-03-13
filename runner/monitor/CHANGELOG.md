### monitor {version} {:#monitor-{version}}

{{date}}

`androidx.test:monitor:{version}` is released.

**Bug Fixes**

**New Features**

**Breaking Changes**

**API Changes**
* Make DeviceController an public API from ExperimentalTestApi
* Upstream TestStorage.isTestStoragePath to PlatformTestStorage
* Upstream TestStorage.getInputFileUri and getOutputFileUri  to PlatformTestStorage
* Change PlatformTestStorage methods to throw FileNotFoundException instead of 
  IOException
* Add internal ControlledLooper#isDrawCallbacksSupported.

**Breaking API Changes**

**Known Issues**
