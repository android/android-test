### Core Core-ktx {version} {:#core-{version}}

{{date}}

`androidx.test:core:{version}` and `androidx.test:core-ktx:{version}` are released.

**Bug Fixes**

* make ViewCapture use ControlledLooper API instead of hardcoding is Robolectric check

**New Features**

**Breaking Changes**

**API Changes**

* Added ApplicationInfoBuilder.setFlags(int)
* Make suspend function versions of ViewCapture/WindowCapture/DeviceCapture APIs
* Make Bitmap.writeToTestStorage use the registered PlatformTestStorage instead of hardcoding TestStorage
* Add *Async variants of capture*ToBitmap methods


**Breaking API Changes**

**Known Issues**
