# Description: Build rules for building androidx.test from source

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:release.bzl", "axt_release_lib")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:axt_versions.bzl", "ANDROIDX_ANNOTATION_VERSION", "ANDROIDX_TRACING_VERSION", "ANNOTATION_VERSION", "JUNIT_VERSION", "MONITOR_VERSION", "RUNNER_VERSION", "SERVICES_VERSION")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

# all users should reference the equivalent targets in //runner/android_junit_runner
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

android_library(
    name = "runner",
    srcs = glob(
        ["**/*.java"],
    ),
    custom_package = "androidx.test.runner",
    idl_srcs = glob(["**/*.aidl"]),
    manifest = "AndroidManifest.xml",
    proguard_specs = [
        "proguard_library.cfg",
    ],
    exports = [
        "//runner/monitor",
    ],
    deps = [
        "//annotation",
        "//runner/monitor",
        "//services/events/java/androidx/test/services/events",
        "//services/storage",
        "@androidsdk//:legacy_test-30",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_tracing_tracing",
        "@maven//:junit_junit",
    ],
)

# group of targets to use to produce release binary + docs
android_library(
    name = "runner_release_lib",
    visibility = ["//visibility:private"],
    exports = [
        ":runner",
    ],
)

# Generate rules for the release artifacts. This generates three targets
# genrule output: runner_release_no_deps.jar and runner_release.aar
# a java_library target librunner_release_no_deps
axt_release_lib(
    name = "runner_release",
    custom_package = "androidx.test.runner",
    jarjar_rules = ["release_jarjar_rules.txt"],
    keep_spec = "androidx/test",
    overlapping_jars = [
        "//annotation/java/androidx/test/annotation:annotation_release_no_deps.jar",
        "//runner/monitor/java/androidx/test:monitor_release_no_deps.jar",
        "//services/storage/java/androidx/test/services/storage:test_storage_release_no_deps.jar",
    ],
    proguard_library = ":proguard_library.cfg",
    # remove the generated R class
    remove_spec = "androidx/test/runner/R[$$\\.]",
    deps = [
        ":runner_release_lib",
    ],
)

dackka_test(
    name = "runner_doc",
    jar = ":runner_release_all_deploy.jar",
    src_jar = ":librunner-src.jar",
)

maven_artifact(
    name = "runner_maven_artifact",
    src = ":runner_release.aar",
    artifact_deps = [
        "androidx.annotation:annotation:%s" % ANDROIDX_ANNOTATION_VERSION,
        "androidx.test:annotation:%s" % ANNOTATION_VERSION,
        # runner depends on exact version of monitor - since it depends on implementation details
        "androidx.test:monitor:[%s]" % MONITOR_VERSION,
        "androidx.test.services:storage:[%s]" % SERVICES_VERSION,
        "androidx.tracing:tracing:%s" % ANDROIDX_TRACING_VERSION,
        "junit:junit:%s" % JUNIT_VERSION,
    ],
    artifact_id = "runner",
    group_id = "androidx.test",
    last_updated = "20170622000000",
    src_jar = ":librunner-src.jar",
    version = "%s" % RUNNER_VERSION,
)

api_checks(
    name = "runner_api",
    deploy_jar = ":runner_release_all_deploy.jar",
    src_jar = ":librunner-src.jar",
)
