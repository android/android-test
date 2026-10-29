### monitor {version} {:#monitor-{version}}

{{date}}

`androidx.test:monitor:{version}` is released.

**Bug Fixes**

* Use `SystemClock.uptimeMillis()` instead of `System.currentTimeMillis()` in
  `MonitoringInstrumentation` so wall-clock adjustments (such as NTP sync) do
  not skew activity completion wait loops or idle timestamps. (b/578740191)

**New Features**

**Breaking Changes**

**API Changes**

**Breaking API Changes**

**Known Issues**
