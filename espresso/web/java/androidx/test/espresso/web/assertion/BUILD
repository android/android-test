# Description:
#   Espresso Web Assertion

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
    deps = [
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//espresso/web/java/androidx/test/espresso/web/action",
        "//espresso/web/java/androidx/test/espresso/web/model",
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_ccil_cowan_tagsoup_tagsoup",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
