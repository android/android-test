# Description:
#  Matchers for Espresso!

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

android_library(
    name = "matcher",
    srcs = glob(
        ["*.java"],
    ),
    exports = [
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
    deps = [
        "//annotation",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//espresso/core/java/androidx/test/espresso/util",
        "//runner/android_junit_runner",
        "@maven//:androidx_annotation_annotation",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)
