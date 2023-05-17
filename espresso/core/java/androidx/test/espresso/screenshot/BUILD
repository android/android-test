# This package contains bitmap capturing extensions for espresso
load("//build_extensions:kt_android_library.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//espresso/core/java/androidx/test/espresso:espresso_pkg"],
)

kt_android_library(
    name = "screenshot",
    srcs = glob(["*.kt"]),
    deps = [
        "//annotation",
        "//core/java/androidx/test/core:core_not_test_only",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso:view-interaction",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_concurrent_concurrent_futures",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
