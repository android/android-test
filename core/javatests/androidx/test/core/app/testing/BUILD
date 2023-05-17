# Description: Stub classes for testing androidx.test.core.app

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:kt_android_library.bzl", "kt_android_library")

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

kt_android_library(
    name = "testing",
    testonly = 1,
    srcs = glob([
        "**/*.java",
        "**/*.kt",
    ]),
    manifest = "AndroidManifest_empty.xml",
    resource_files = glob(["res/**"]),
    visibility = ["//visibility:public"],
    deps = [
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
    ],
)

# keep the activity manifest entries in a separate target, so they are not
# present in both binary_under_test and test apk
android_library(
    name = "manifest",
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    visibility = ["//visibility:public"],
)
