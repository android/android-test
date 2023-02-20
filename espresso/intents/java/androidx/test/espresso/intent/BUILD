# Description:
#   Common library for testing inter and intra app communication via intents.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:release.bzl", "axt_release_lib")
load("//build_extensions:maven_repo.bzl", "maven_artifact")
load("//build_extensions:axt_versions.bzl", "ANNOTATION_VERSION", "CORE_VERSION", "ESPRESSO_VERSION", "KOTLIN_VERSION", "RULES_VERSION")
load("//build_extensions:combine_jars.bzl", "combine_jars")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//:__subpackages__"],
)

android_library(
    name = "intents",
    testonly = 1,
    srcs = glob(
        ["*.java"],
        exclude = [
            "ResolvedIntent.java",
            "Checks.java",
        ],
    ),
    visibility = ["//visibility:public"],
    deps = [
        ":precondition_checks",
        ":resolved_intent_interface",
        "//annotation",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher",
        "//opensource:hamcrest",
        "//runner/android_junit_runner",
        "@androidsdk//:legacy_test-30",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_errorprone_error_prone_annotations",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

android_library(
    name = "precondition_checks",
    srcs = [
        "Checks.java",
    ],
)

android_library(
    name = "resolved_intent_interface",
    srcs = ["ResolvedIntent.java"],
)

# ** Generate the release artifacts **

android_library(
    name = "espresso_intents_release_lib",
    testonly = 1,
    exports = [
        ":intents",
        "//espresso/intents/java/androidx/test/espresso/intent/rule",
    ],
)

# Generates the release artifacts
axt_release_lib(
    name = "espresso_intents_release",
    keep_spec = "androidx/test/espresso/intent",
    remove_spec = "androidx/test/espresso/intent/R[$$\\.]",
    deps = [
        ":espresso_intents_release_lib",
    ],
)

filegroup(
    name = "intents_src",
    srcs = [
        ":libintents-src.jar",
        ":libprecondition_checks-src.jar",
        ":libresolved_intent_interface-src.jar",
        "//espresso/intents/java/androidx/test/espresso/intent/matcher:libmatcher-src.jar",
        "//espresso/intents/java/androidx/test/espresso/intent/rule:librule-src.jar",
    ],
)

combine_jars(
    name = "intents_src_jars",
    srcs = [
        ":intents_src",
    ],
)

dackka_test(
    name = "espresso_intents_doc",
    jar = ":espresso_intents_release_all_deploy.jar",
    src_jar = ":intents_src_jars.jar",
)

maven_artifact(
    name = "espresso_intents_maven_artifact",
    src = ":espresso_intents_release.aar",
    artifact_deps = [
        "androidx.test.espresso:espresso-core:%s" % ESPRESSO_VERSION,
        "androidx.test:core:%s" % CORE_VERSION,
        "androidx.test:rules:%s" % RULES_VERSION,
        "androidx.test:annotation:%s" % ANNOTATION_VERSION,
        "org.jetbrains.kotlin:kotlin-stdlib:%s" % KOTLIN_VERSION,
    ],
    artifact_id = "espresso-intents",
    group_id = "androidx.test.espresso",
    last_updated = "20170622000000",
    src_jar = ":intents_src_jars.jar",
    version = "%s" % ESPRESSO_VERSION,
)

api_checks(
    name = "espresso_intents_api",
    deploy_jar = ":espresso_intents_release_all_deploy.jar",
    src_jar = ":intents_src_jars.jar",
)
