# Description: Build rules for building androidx.test from source

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:kt_android_library.bzl", "kt_android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

# nearly all users should reference the equivalent target in //runner/monitor
package(
    default_applicable_licenses = ["//:license"],
    default_visibility = [
        ":allowlist",
    ],
)

package_group(
    name = "allowlist",
    packages = [
        "//...",
    ],
)

licenses(["notice"])

kt_android_library(
    name = "monitor",
    srcs = glob(
        [
            "**/*.java",
            "**/*.kt",
        ],
        exclude = [
            "internal/runner/runtime/ExposedInstrumentationApi.java",
            "internal/runner/hidden/ExposedInstrumentationApi.java",
            "internal/platform/app/ActivityInvoker$$CC.java",
        ],
    ),
    tags = ["alt_dep=//runner/monitor"],
    deps = [
        ":compiletime_hidden_apis",
        ":errorprone_neverlink",
        ":runtime_hidden_apis",
        "//annotation",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_tracing_tracing",
    ],
)

# avoid the runtime dependency on errorprone
java_library(
    name = "errorprone_neverlink",
    neverlink = 1,
    exports = [
        "@maven//:com_google_errorprone_error_prone_annotations",
    ],
)

android_library(
    name = "runtime_hidden_apis",
    srcs = [
        "internal/runner/runtime/ExposedInstrumentationApi.java",
    ],
    deps = ["@maven//:androidx_annotation_annotation"],
)

android_library(
    name = "compiletime_hidden_apis",
    srcs = [
        "internal/runner/hidden/ExposedInstrumentationApi.java",
    ],
    neverlink = 1,
    deps = [
        "@maven//:androidx_annotation_annotation",
    ],
)

# group of targets to use to produce release binary + docs
android_library(
    name = "monitor_release_lib",
    srcs = [
        # only needed for external release backwards compatibility
        "internal/platform/app/ActivityInvoker$$CC.java",
    ],
    custom_package = "androidx.test.monitor",
    manifest = "AndroidManifest.xml",
    proguard_specs = [":proguard_library.cfg"],
    visibility = ["//visibility:private"],
    exports = [
        ":monitor",
    ],
    deps = [
        ":monitor",
        "@maven//:androidx_annotation_annotation",
    ],
)

axt_android_aar(
    name = "monitor_aar",
    # Unfortunately this legacy artifact doesn't follow standard class naming conventions.
    # so we'll be more explicit in the expected packages list to catch new classes that don't follow
    # expected package patterns
    expected_class_prefixes = [
        "androidx.test.InstrumentationRegistry",
        "androidx.test.annotation.Beta",
        "androidx.test.internal",
        "androidx.test.platform",
        "androidx.test.runner",
    ],
    included_dep = ":monitor_release_lib",
)

maven_artifact(
    name = "monitor_maven_artifact",
    last_updated = "20170622000000",
    target = ":monitor_aar",
)

dackka_test(
    name = "monitor_doc",
    runtime_dep = ":monitor_release_lib",
    src_jar = ":libmonitor-src.jar",
)

api_checks(
    name = "monitor_api",
    runtime_dep = ":monitor_release_lib",
    # ideally we would use monitor_aar-src.jar here, but metalava complains
    # about ExposedInstrumentationApi's package
    src_jar = ":libmonitor-src.jar",
)
