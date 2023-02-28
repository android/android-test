load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

android_library(
    name = "sugar",
    srcs = glob(
        ["*.java"],
    ),
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//espresso/web/java/androidx/test/espresso/web/action",
        "//espresso/web/java/androidx/test/espresso/web/assertion",
        "//espresso/web/java/androidx/test/espresso/web/model",
        "//runner/android_junit_runner",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
