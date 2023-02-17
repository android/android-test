load("@io_bazel_rules_kotlin//kotlin:jvm.bzl", "kt_jvm_test")

kt_jvm_test(
    name = "JarCombinerTest",
    srcs = ["JarCombinerTest.kt"],
    data = [
        "//build_extensions/jar_combiner/javatests/androidx/test/tools/jarcombiner/fixtures:libjar1.jar",
        "//build_extensions/jar_combiner/javatests/androidx/test/tools/jarcombiner/fixtures:libjar2.jar",
        "//build_extensions/jar_combiner/javatests/androidx/test/tools/jarcombiner/fixtures:libjar_with_r.jar",
    ],
    deps = [
        "//build_extensions/jar_combiner/java/androidx/test/tools/jarcombiner:jarcombiner_lib",
        "//build_extensions/jar_validator/java/androidx/test/tools/jarvalidator:jarvalidator_lib",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
