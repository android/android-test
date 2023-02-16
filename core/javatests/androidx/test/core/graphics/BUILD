# Description: Tests for androidx.test.core

load("//build_extensions:android_library_test.bzl", "axt_android_library_test")

package(
    default_testonly = 1,
)

licenses(["notice"])

axt_android_library_test(
    name = "BitmapStorageTest_android",
    srcs =
        ["BitmapStorageTest.kt"],
    deps = [
        "//core",
        "//ext/junit",
        "//runner/monitor",
        "@maven//:junit_junit",
    ],
)
