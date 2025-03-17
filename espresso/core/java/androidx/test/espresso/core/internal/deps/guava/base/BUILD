# Description:
#    Redefinition of repackaged guava classes that were mistakenly exposed in public EspressoOptional API

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
    default_visibility = ["//espresso/core/java/androidx/test/espresso:espresso_pkg"],
)

android_library(
    name = "base",
    testonly = 0,
    srcs = glob(["*java"]),
    deps = [
        "//opensource/androidx:annotation",
        "//runner/monitor",
    ],
)
