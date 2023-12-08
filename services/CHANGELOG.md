### services {version} {:#services-{version}}

{{date}}

`androidx.test.services:test-services:{version}` `androidx.test.services:storage:{version}` are released.

**Bug Fixes**

* When files are opened for writing, TestStorage now truncates the file unless
it is explicitly opened for appending. This prevents bytes from a prior write
to the file from remaining at the end of the file.

**New Features**

**Breaking Changes**

**API Changes**

**Breaking API Changes**

**Known Issues**
