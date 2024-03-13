# Description:
#  View Actions for espresso.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:kt_android_library.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

ADAPTER_VIEW_PROTOCOL = [
    "AdapterViewProtocols.java",
    "AdapterViewProtocol.java",
]

COMMON_JAVACOPTS = [
    "-Xep:ObjectToString:OFF",  # See http://go/objecttostring-lsc
]

android_library(
    name = "adapter_view_protocol",
    srcs = ADAPTER_VIEW_PROTOCOL,
    deps = [
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/util",
        "//opensource/androidx:annotation",
        "//runner/monitor",
    ],
)

kt_android_library(
    name = "action",
    srcs = glob(
        [
            "*.java",
            "*.kt",
        ],
        exclude = ADAPTER_VIEW_PROTOCOL,
    ),
    javacopts = COMMON_JAVACOPTS,
    deps = [
        ":adapter_view_protocol",
        "//annotation/java/androidx/test/annotation",
        "//core/java/androidx/test/core:core_not_test_only",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//espresso/core/java/androidx/test/espresso/util",
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource",
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
