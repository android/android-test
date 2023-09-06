load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

package(
    default_visibility = [
        "//:__subpackages__",
    ],
)

kt_jvm_library(
    name = "zipcombiner_lib",
    srcs = glob([
        "*.kt",
    ]),
)

java_binary(
    name = "zipcombiner",
    srcs = ["Main.java"],
    main_class = "androidx.test.tools.zipcombiner.Main",
    deps = [
        ":zipcombiner_lib",
    ],
)
