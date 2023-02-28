# Description:
#  View Assertions for New UI Test API.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

android_library(
    name = "assertion",
    srcs = glob(
        ["*.java"],
    ),
    exports = [
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
    deps = [
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//espresso/core/java/androidx/test/espresso/util",
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
