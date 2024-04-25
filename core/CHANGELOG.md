### Core Core-ktx {version} {:#core-{version}}

{{date}}

`androidx.test:core:{version}` and `androidx.test:core-ktx:{version}` are released.

**Bug Fixes**

* make ViewCapture use ControlledLooper API instead of hardcoding is Robolectric check

**New Features**

**Breaking Changes**

**API Changes**

* Added ApplicationInfoBuilder.setFlags(int)
* Make suspend function versions of ViewCapture/WindowCapture/DeviceCapture APIs,  
  and rename existing methods as *Async variants that return ListenableFutures
* Make Bitmap.writeToTestStorage use the registered PlatformTestStorage instead of hardcoding TestStorage
* Remove ExperimentalTestApi/RequiresOptIn restrictions from captureToBitmap and takeScreenshot APIs

**Breaking API Changes**

**Known Issues**
