# Description: Tests for androidx.test.core

load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")
load("//build_extensions:android_library_test.bzl", "axt_android_library_test")

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_local_test(
    name = "ParcelablesTest",
    srcs =
        ["ParcelablesTest.java"],
    deps = [
        "//core",
        "//ext/junit",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_library_test(
    name = "ParcelablesTest_android",
    srcs =
        ["ParcelablesTest.java"],
    deps = [
        "//core",
        "//ext/junit",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
