# Top level BUILD file for GitHub

package(default_visibility = ["//:__subpackages__"])

exports_files(["repo.bzl"])

java_test(
    name = "instrumentation_test_runner",
    testonly = 1,
    tags = ["manual"],
    test_class = "com.google.android.apps.common.testing.suite.AndroidDeviceTestSuite",
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//opensource:entry_point_import",
    ],
)
