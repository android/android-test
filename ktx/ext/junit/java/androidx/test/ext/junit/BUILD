# androidx.ext.junit Kotlin extensions

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:kt_android_library.bzl", "kt_android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_visibility = ["//:__subpackages__"],
)

kt_android_library(
    name = "junit",
    testonly = 1,
    srcs = glob(["**/*.kt"]),
    deps = [
        "//ext/junit",
    ],
)

# Group of targets to use to produce release binary
# kt_android_library doesn't produce an aar, so wrap inside a android_library
android_library(
    name = "aar_lib",
    testonly = 1,
    custom_package = "androidx.test.ext.junit.ktx",
    manifest = "AndroidManifest.xml",
    visibility = ["//visibility:private"],
    exports = [
        ":junit",
    ],
)

# Generate rules for the release artifacts.
axt_android_aar(
    name = "junit_aar",
    testonly = 1,
    # we'll be super explicit with the expected classes here since this is effectively a
    # stale artifact. All future classes should just go into //ext/junit
    expected_class_prefixes = [
        "androidx.test.ext.junit.rules.ActivityScenarioRuleKt",
    ],
    included_dep = ":aar_lib",
)

maven_artifact(
    name = "junit_maven_artifact",
    testonly = 1,
    last_updated = "20181029000000",
    target = ":junit_aar",
)

dackka_test(
    name = "junit_doc",
    runtime_dep = ":aar_lib",
    src_jar = ":junit_aar-src.jar",
)

api_checks(
    name = "junit_api",
    runtime_dep = ":aar_lib",
    src_jar = ":junit_aar-src.jar",
)
