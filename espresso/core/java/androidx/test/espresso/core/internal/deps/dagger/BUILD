# Description:
#   Internal package that will contain shaded dagger classes for external release

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_visibility = ["//espresso/core/java/androidx/test/espresso:__pkg__"],
)

java_library(
    name = "dagger",
    srcs = glob(["*java"]),
    deps = [
        "@maven//:androidx_annotation_annotation",
    ],
)
