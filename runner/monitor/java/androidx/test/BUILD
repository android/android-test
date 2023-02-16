# Description: Build rules for building androidx.test from source

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")
load("//build_extensions:release.bzl", "axt_release_lib")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:axt_versions.bzl", "ANDROIDX_ANNOTATION_VERSION", "ANDROIDX_TRACING_VERSION", "ANNOTATION_VERSION", "KOTLIN_VERSION", "MONITOR_VERSION")
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
    visibility = ["//visibility:private"],
    exports = [
        ":monitor",
    ],
    deps = [
        ":monitor",
        "@maven//:androidx_annotation_annotation",
    ],
)

# Generate rules for the release artifacts. This generates three targets
# genrule output: monitor_release_no_deps.jar and monitor_release.aar
# a java_library target librunner_release_no_deps
axt_release_lib(
    name = "monitor_release",
    custom_package = "androidx.test.monitor",
    jarjar_rules = ["release_jarjar_rules.txt"],
    keep_spec = "androidx/test",
    overlapping_jars = [
        "//annotation/java/androidx/test/annotation:annotation_release_no_deps.jar",
    ],
    proguard_library = ":proguard_library.cfg",
    # remove the generated R class
    remove_spec = "androidx/test/monitor/R[$$\\.]",
    deps = [
        ":monitor_release_lib",
    ],
)

dackka_test(
    name = "monitor_doc",
    jar = ":monitor_release_all_deploy.jar",
    src_jar = ":libmonitor-src.jar",
)

maven_artifact(
    name = "monitor_maven_artifact",
    src = ":monitor_release.aar",
    artifact_deps = [
        "androidx.annotation:annotation:%s" % ANDROIDX_ANNOTATION_VERSION,
        "androidx.test:annotation:%s" % ANNOTATION_VERSION,
        "androidx.tracing:tracing:%s" % ANDROIDX_TRACING_VERSION,
        "org.jetbrains.kotlin:kotlin-stdlib:%s" % KOTLIN_VERSION,
    ],
    artifact_id = "monitor",
    group_id = "androidx.test",
    last_updated = "20170622000000",
    src_jar = ":libmonitor-src.jar",
    version = "%s" % MONITOR_VERSION,
)

api_checks(
    name = "monitor_api",
    deploy_jar = ":monitor_release_all_deploy.jar",
    src_jar = ":libmonitor-src.jar",
)
