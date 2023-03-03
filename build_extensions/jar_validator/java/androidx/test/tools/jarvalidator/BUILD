load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_library")

kt_jvm_library(
    name = "jarvalidator_lib",
    srcs = glob([
        "*.kt",
    ]),
    visibility = [
        "//:__subpackages__",
    ],
)

java_binary(
    name = "jarvalidator",
    srcs = ["Main.java"],
    main_class = "androidx.test.tools.jarvalidator.Main",
    visibility = [
        "//:__subpackages__",
    ],
    deps = [
        ":jarvalidator_lib",
    ],
)
