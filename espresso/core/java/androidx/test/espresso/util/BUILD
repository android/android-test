# Description:
#  Utility classes

load("//build_extensions:kt_android_library.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

kt_android_library(
    name = "util",
    srcs = glob([
        "*.java",
        "*.kt",
    ]),
    deps = [
        "//espresso/core/java/androidx/test/espresso/core/internal/deps/guava/base",
        "//runner/android_junit_runner",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven_listenablefuture//:com_google_guava_listenablefuture",
    ],
)
