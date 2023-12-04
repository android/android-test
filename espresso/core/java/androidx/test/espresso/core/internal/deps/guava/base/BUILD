# Description:
#    Redefinition of repackaged guava classes that were mistakenly exposed in public EspressoOptional API

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_visibility = ["//espresso/core/java/androidx/test/espresso:espresso_pkg"],
)

android_library(
    name = "base",
    srcs = glob(["*java"]),
    deps = [
        "//opensource/androidx:annotation",
        "//runner/monitor",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)
