load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:release.bzl", "axt_release_lib")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:axt_versions.bzl", "ANNOTATION_VERSION", "RULES_VERSION", "RUNNER_VERSION")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

# Description: Build rules for building androidx.test from source
licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_visibility = [
        "//:__subpackages__",
    ],
)

# test rules
android_library(
    name = "rules",
    srcs = glob(["**/*.java"]),
    custom_package = "androidx.test.rules",
    manifest = "AndroidManifest.xml",
    proguard_specs = [
        "proguard.cfg",
    ],
    deps = [
        "//annotation",
        "//runner/android_junit_runner",
        "@androidsdk//:legacy_test-30",
        "@maven//:androidx_annotation_annotation",
        "@maven//:junit_junit",
    ],
)

# Generate rules for the release artifacts. This generates two file: runner_release_no_dep.jar
# and runner_release.aar
axt_release_lib(
    name = "rules_release",
    custom_package = "androidx.test.rules",
    keep_spec = "androidx/test",
    overlapping_jars = [
        "//annotation/java/androidx/test/annotation:annotation_release_no_deps.jar",
        "//runner/android_junit_runner/java/androidx/test:runner_release_no_deps.jar",
        "//runner/monitor/java/androidx/test:monitor_release_no_deps.jar",
        "//services/storage/java/androidx/test/services/storage:test_storage_release_no_deps.jar",
    ],
    proguard_library = "proguard.cfg",
    # remove the generated R class
    remove_spec = "androidx/test/rules/R[$$\\.]",
    deps = [
        ":rules",
    ],
)

dackka_test(
    name = "rules_doc",
    runtime_dep = ":rules",
    src_jar = ":librules-src.jar",
)

maven_artifact(
    name = "rules_maven_artifact",
    src = ":rules_release.aar",
    artifact_deps = [
        "androidx.test:runner:%s" % RUNNER_VERSION,
        "androidx.test:annotation:%s" % ANNOTATION_VERSION,
    ],
    artifact_id = "rules",
    group_id = "androidx.test",
    last_updated = "20170622000000",
    src_jar = ":librules-src.jar",
    version = "%s" % RULES_VERSION,
)

api_checks(
    name = "rules_api",
    runtime_dep = ":rules",
    src_jar = ":librules-src.jar",
)
