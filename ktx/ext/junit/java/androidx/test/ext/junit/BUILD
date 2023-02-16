# androidx.ext.junit Kotlin extensions

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")
load("//build_extensions:release.bzl", "axt_release_lib")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:axt_versions.bzl", "ANDROIDX_JUNIT_VERSION", "ANDROIDX_LIFECYCLE_VERSION", "KOTLIN_VERSION")
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
    custom_package = "androidx.test.ext.junit.ktx",
    keep_spec = "androidx/test/ext/junit|META-INF/androidx.test.ext.junit.kotlin_module",
    overlapping_jars = [
        "//ext/junit/java/androidx/test/ext/junit:junit_release_no_deps.jar",
    ],
    remove_spec = "androidx/test/ext/junit/ktx/R[$$\\.]|androidx/test/ext/junit/R[$$\\.]",
    resource_files = glob(
        ["res/**/*"],
    ),
    deps = [
        ":junit_release_lib",
    ],
)

dackka_test(
    name = "junit_doc",
    jar = ":junit_release_all_deploy.jar",
    src_jar = ":libjunit-src.jar",
)

maven_artifact(
    name = "junit_maven_artifact",
    src = ":junit_release.aar",
    artifact_deps = [
        "androidx.test.ext:junit:%s" % ANDROIDX_JUNIT_VERSION,
        "androidx.lifecycle:lifecycle-common:%s" % ANDROIDX_LIFECYCLE_VERSION,
        "org.jetbrains.kotlin:kotlin-stdlib:%s" % KOTLIN_VERSION,
    ],
    artifact_id = "junit-ktx",
    group_id = "androidx.test.ext",
    last_updated = "20181029000000",
    src_jar = ":libjunit-src.jar",
    version = "%s" % ANDROIDX_JUNIT_VERSION,
)

api_checks(
    name = "junit_api",
    deploy_jar = ":junit_release_all_deploy.jar",
    src_jar = ":libjunit-src.jar",
)
