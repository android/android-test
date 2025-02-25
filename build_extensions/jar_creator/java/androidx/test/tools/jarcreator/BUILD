load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

package(
    default_visibility = [
        "//:__subpackages__",
    ],
)

kt_jvm_library(
    name = "jarcreator_lib",
    srcs = glob([
        "*.kt",
    ]),
)

java_binary(
    name = "jarcreator",
    srcs = ["Main.java"],
    main_class = "androidx.test.tools.jarcreator.Main",
    deps = [
        ":jarcreator_lib",
    ],
)
