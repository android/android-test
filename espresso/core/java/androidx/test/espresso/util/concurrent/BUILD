# Description:
#  Utility classes

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
    default_visibility = ["//espresso:__subpackages__"],
)

android_library(
    name = "concurrent",
    srcs = glob([
        "*.java",
        "*.kt",
    ]),
    deps = [
        "//opensource/androidx:annotation",
        "//runner/monitor",
        "@maven_listenablefuture//:com_google_guava_listenablefuture",
    ],
)
