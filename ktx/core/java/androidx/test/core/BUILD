# androidx.test.core Kotlin extensions

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:kt_android_library.bzl", "kt_android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions:api_checks.bzl", "api_checks")
load("//build_extensions:dackka_test.bzl", "dackka_test")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_visibility = ["//:__subpackages__"],
)

kt_android_library(
    name = "core",
    testonly = 1,
    srcs = glob(["**/*.kt"]),
    custom_package = "androidx.test.core.ktx",
    manifest = "AndroidManifest.xml",
    deps = [
        "//core",
        "@maven//:androidx_lifecycle_lifecycle_common",
    ],
)

# Group of targets to use to produce release binary
# kt_android)library doesn't produce an aar, so wrap inside a android_library
android_library(
    name = "core_aar_lib",
    testonly = 1,
    custom_package = "androidx.test.core.ktx",
    manifest = "AndroidManifest.xml",
    visibility = ["//visibility:private"],
    exports = [
        ":core",
    ],
)

axt_android_aar(
    name = "core_aar",
    testonly = 1,
    # we'll be super explicit with the expected classes here since this is effectively a
    # stale artifact. All future classes should just go into //core
    expected_class_prefixes = [
        "androidx.test.core.app.ActivityScenarioKt",
    ],
    included_dep = ":core_aar_lib",
)

maven_artifact(
    name = "core_maven_artifact",
    testonly = 1,
    last_updated = "20181029000000",
    target = ":core_aar",
)

dackka_test(
    name = "core_doc",
    runtime_dep = ":core_aar_lib",
    src_jar = ":core_aar-src.jar",
)

api_checks(
    name = "core_api",
    runtime_dep = ":core_aar_lib",
    src_jar = ":core_aar-src.jar",
)
