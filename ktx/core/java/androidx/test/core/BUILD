# androidx.test.core Kotlin extensions

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")
load("//build_extensions:release.bzl", "axt_release_lib")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:axt_versions.bzl", "ANDROIDX_LIFECYCLE_VERSION", "CORE_VERSION", "KOTLIN_VERSION")
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
android_library(
    name = "core_release_lib",
    testonly = 1,
    visibility = ["//:__subpackages__"],
    exports = [
        ":core",
    ],
)

# Generate rules for the release artifacts. This generates three targets
# genrule output: core_release_no_deps.jar and core_release.aar
# a java_library target libcore_release_no_deps
axt_release_lib(
    name = "core_release",
    custom_package = "androidx.test.core.ktx",
    keep_spec = "androidx/test/core|META-INF/androidx.test.core.ktx.kotlin_module",
    overlapping_jars = [
        "//core/java/androidx/test/core:core_release_no_deps.jar",
    ],
    remove_spec = "androidx/test/core/ktx/R[$$\\.]|androidx/test/core/R[$$\\.]",
    resource_files = glob(
        ["res/**/*"],
    ),
    deps = [
        ":core_release_lib",
    ],
)

dackka_test(
    name = "core_doc",
    jar = ":core_release_all_deploy.jar",
    src_jar = ":libcore-src.jar",
)

maven_artifact(
    name = "core_maven_artifact",
    src = ":core_release.aar",
    artifact_deps = [
        "androidx.test:core:%s" % CORE_VERSION,
        "androidx.lifecycle:lifecycle-common:%s" % ANDROIDX_LIFECYCLE_VERSION,
        "org.jetbrains.kotlin:kotlin-stdlib:%s" % KOTLIN_VERSION,
    ],
    artifact_id = "core-ktx",
    group_id = "androidx.test",
    last_updated = "20181029000000",
    src_jar = ":libcore-src.jar",
    version = "%s" % CORE_VERSION,
)

api_checks(
    name = "core_api",
    deploy_jar = ":core_release_all_deploy.jar",
    src_jar = ":libcore-src.jar",
)
