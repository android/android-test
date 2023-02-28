load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

licenses(["notice"])

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
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//espresso/web/java/androidx/test/espresso/web/bridge",
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
