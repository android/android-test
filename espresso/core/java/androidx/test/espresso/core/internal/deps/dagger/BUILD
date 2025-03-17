# Description:
#   Internal package that will contain shaded dagger classes for external release

load("@rules_java//java:defs.bzl", "java_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
    default_visibility = ["//espresso/core/java/androidx/test/espresso:__pkg__"],
)

java_library(
    name = "dagger",
    srcs = glob(["*java"]),
    deps = [
        "//opensource/androidx:annotation",
    ],
)
