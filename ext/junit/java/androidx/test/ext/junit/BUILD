# Description: Build rules for building androidx.test.junit from source

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:release.bzl", "axt_release_lib")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:axt_versions.bzl", "ANDROIDX_ANNOTATION_VERSION", "ANDROIDX_JUNIT_VERSION", "CORE_VERSION", "JUNIT_VERSION", "MONITOR_VERSION")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

# all users should reference the equivalent targets in //ext/junit
package(
    default_applicable_licenses = ["//:license"],
    default_visibility = ["//:__subpackages__"],
)

licenses(["notice"])

android_library(
    name = "junit",
    testonly = 1,
    srcs = glob(
        ["**/*.java"],
    ),
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
    tags = ["alt_dep=//ext/junit"],
    deps = [
        "//core",
        "//runner/monitor",
        "//services/storage",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_android_apps_common_testing_accessibility_framework_accessibility_test_framework/internal/passivescan",
        "@maven//:junit_junit",
    ],
)

# group of targets to use to produce release binary + docs
android_library(
    name = "junit_release_lib",
    testonly = 1,
    visibility = ["//:__subpackages__"],
    exports = [
        ":junit",
    ],
)

# Generate rules for the release artifacts. This generates three targets
# genrule output: junit_release_no_deps.jar and junit_release.aar
# a java_library target libjunit_release_no_deps
axt_release_lib(
    name = "junit_release",
    keep_spec = "androidx/test/ext/junit",
    remove_spec = "androidx/test/ext/junit/R[$$\\.]",
    deps = [
        ":junit_release_lib",
    ],
)

dackka_test(
    name = "junit_doc",
    runtime_dep = ":junit_release_lib",
    src_jar = ":libjunit-src.jar",
)

maven_artifact(
    name = "junit_maven_artifact",
    src = ":junit_release.aar",
    artifact_deps = [
        "junit:junit:%s" % JUNIT_VERSION,
        "androidx.test:core:%s" % CORE_VERSION,
        "androidx.test:monitor:%s" % MONITOR_VERSION,
        "androidx.annotation:annotation:%s" % ANDROIDX_ANNOTATION_VERSION,
    ],
    artifact_id = "junit",
    group_id = "androidx.test.ext",
    last_updated = "20180723000000",
    src_jar = ":libjunit-src.jar",
    version = "%s" % ANDROIDX_JUNIT_VERSION,
)

api_checks(
    name = "junit_api",
    runtime_dep = ":junit_release_lib",
    src_jar = ":libjunit-src.jar",
)
