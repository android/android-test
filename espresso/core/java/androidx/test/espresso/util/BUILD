# Description:
#  Utility classes

load("//build_extensions:kt_android_library.bzl", "kt_android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
    default_visibility = ["//visibility:public"],
)

kt_android_library(
    name = "util",
    testonly = 0,
    srcs = glob([
        "*.java",
        "*.kt",
    ]),
    deps = [
        "//espresso/core/java/androidx/test/espresso/core/internal/deps/guava/base",
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven_listenablefuture//:com_google_guava_listenablefuture",
    ],
)
