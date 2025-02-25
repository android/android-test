load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")

kt_jvm_test(
    name = "JarCreatorTest",
    srcs = ["JarCreatorTest.kt"],
    deps = [
        "//build_extensions/jar_creator/java/androidx/test/tools/jarcreator:jarcreator_lib",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
