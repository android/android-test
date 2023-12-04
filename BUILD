load("//build_extensions/maven:maven_repo.bzl", "maven_repository")
load("@rules_jvm_external//:defs.bzl", "artifact")
load("@rules_license//rules:license.bzl", "license")

package(default_visibility = ["//:__subpackages__"])

exports_files([
    "proguard_binary.cfg",
    "LICENSE",
    "repo.bzl",
])

# Creates maven release repository
maven_repository(
    name = "axt_m2repository",
    testonly = 1,
    srcs = [
        "//core/java/androidx/test/core:core_maven_artifact",
        "//ktx/core/java/androidx/test/core:core_maven_artifact",
    ],
)

java_test(
    name = "instrumentation_test_runner",
    testonly = 1,
    tags = ["manual"],
    test_class = "com.google.android.apps.common.testing.suite.AndroidDeviceTestSuite",
    visibility = ["//visibility:public"],
    runtime_deps = [
        "//opensource:entry_point_import",
    ],
)

license(name = "license")
