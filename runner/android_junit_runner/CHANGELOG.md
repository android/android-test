### runner {version} {:#runner-{version}}

{{date}}

`androidx.test:runner:{version}` is released.

**Bug Fixes**

* Exceptions during `@AfterClass` were not being reported via `InstrumentationResultPrinter`.
* Exceptions arising in AndroidJUnitRunner.buildRequest are now handled.
* Assumption failures during a ClassRule or BeforeClass are now reported more consistently via `InstrumentationResultPrinter`
* Clarify SdkSuppress reference docs

**New Features**

**Breaking Changes**

**API Changes**

* Update to minSdkVersion 21
* Deprecate androidx.test.filters.Suppress in favor of org.junit.Ignore

**Breaking API Changes**

**Known Issues**
