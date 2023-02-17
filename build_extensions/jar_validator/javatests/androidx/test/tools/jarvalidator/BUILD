load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")

kt_jvm_test(
    name = "JarValidatorTest",
    srcs = ["JarValidatorTest.kt"],
    data = [
        "//build_extensions/jar_validator/javatests/androidx/test/tools/jarvalidator/fixtures:libmatching.jar",
        "//build_extensions/jar_validator/javatests/androidx/test/tools/jarvalidator/fixtures:libnotmatching.jar",
    ],
    deps = [
        "//build_extensions/jar_validator/java/androidx/test/tools/jarvalidator:jarvalidator_lib",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
