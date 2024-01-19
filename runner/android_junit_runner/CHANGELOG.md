### runner {version} {:#runner-{version}}

{{date}}

`androidx.test:runner:{version}` is released.

**Bug Fixes**

* Attempt to clarify limitations and deprecation reasons in RequiresDevice documentation
* Remove all support for Android SDKs < 19. Minimum is API 19 (Android Kit Kat 4.4)
* Fix that "-e class" and "-e notClass" on the same class/method should perform the same result (no tests run)

**New Features**

**Breaking Changes**

**API Changes**

* Mark androidx.test.services.** as RestrictTo LIBRARY_GROUP
* Remove ExperimentalTestApi from CustomFilter - making it public
* Remove ExperimentalTestApi from PackagePrefixClasspathSuite - make it public
* Mark PermissionRequester as RestrictTo LIBRARY_GROUP instead of ExperimentalTestApi

**Breaking API Changes**

**Known Issues**
