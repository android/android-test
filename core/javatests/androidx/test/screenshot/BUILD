# Description: Tests for androidx.test.core

load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_local_test")

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

kt_android_local_test(
    name = "BitmapHolderTest",
    srcs =
        ["BitmapHolderTest.kt"],
    deps = [
        "//core",
        "//core/java/androidx/test/screenshot:bitmap_holder",
        "//ext/junit",
        "//runner/monitor",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
