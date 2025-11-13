# Description: Tests for Runner

load(
    "//build_extensions:axt_android_local_test.bzl",
    "axt_android_local_test",
)

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_local_test(
    name = "TestsRegExFilterTest",
    srcs = [
        "TestsRegExFilterTest.java",
    ],
    deps = [
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner/java/androidx/test:filters",
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "@maven//:com_google_truth_truth",
    ],
)
