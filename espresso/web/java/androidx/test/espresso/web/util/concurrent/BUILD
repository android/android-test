# Description:
#   Espresso Web Actions

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_visibility = ["//espresso/web:__subpackages__"],
)

android_library(
    name = "concurrent",
    srcs = glob(
        ["*.java"],
    ),
    deps = [
        "//opensource/androidx:annotation",
        "//runner/monitor",
        "@maven//:androidx_concurrent_concurrent_futures",
        "@maven_listenablefuture//:com_google_guava_listenablefuture",
    ],
)
