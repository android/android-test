This GitHub project hosts two somewhat distinct projects: 
1. AndroidX Test libraries
2. Bazel support for android_instrumentation_test

# AndroidX Test Libraries

The AndroidX Test Library provides an extensive framework for testing Android apps. This library provides a set of APIs that allow you to quickly build and run test code for your apps, including JUnit 4 and functional user interface (UI) tests. You can run tests created using these APIs from the Android Studio IDE or from the command line.

For more details see [developers.android.com/testing](https://developers.android.com/testing)

The following maven libraries are hosted in this repo:

androidx.test:annotation
androidx.test:core
androidx.test.espresso*
androidx.test.ext:junit
androidx.test:orchestrator
androidx.test:runner
androidx.test:rules
androidx.test:services

androidx.test.uiautomator and androidx.test:ext:junit-gtest are hosted on [AOSP](https://android.googlesource.com/platform/frameworks/support/+/androidx-main/README.md)

## Contributing

See [CONTRIBUTING.md](https://github.com/android/android-test/blob/master/CONTRIBUTING.md)

## Issues

We use the
[GitHub issue tracker](https://github.com/android/android-test/issues) for
tracking feature requests and bugs.

Please see the
[AndroidX Test Discuss mailing list](https://groups.google.com/forum/#!forum/androidx-test-discuss)
for general questions and discussion, and please direct specific questions to
[Stack Overflow](https://stackoverflow.com/questions/tagged/androidx-test).

## Releases

https://developer.android.com/jetpack/androidx/releases/test is the canonical source for release notes, and
https://maven.google.com for release artifacts and source snapshots.

# Bazel android_instrumentation_test support 

To depend on this repository in Bazel, add the following snippet to your
WORKSPACE file:

```
ATS_TAG = "<release-tag>"
http_archive(
    name = "android_test_support",
    sha256 = "<sha256 of release>",
    strip_prefix = "android-test-%s" % ATS_TAG,
    urls = ["https://github.com/android/android-test/archive/%s.tar.gz" % ATS_TAG],
)
load("@android_test_support//:repo.bzl", "android_test_repositories")
android_test_repositories()
```

