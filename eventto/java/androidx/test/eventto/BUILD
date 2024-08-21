load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

android_library(
    name = "eventto",
    testonly = 1,
    srcs = glob(["*.java"]),
    manifest = "AndroidManifest.xml",
    visibility = [
        "//eventto:__pkg__",
    ],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso:espresso_compiletime",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/base",
        "//espresso/core/java/androidx/test/espresso/base:idling_resource_registry",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/util",
        "//opensource/androidx:annotation",
        "//runner/monitor",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:javax_inject_javax_inject",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven_listenablefuture//:com_google_guava_listenablefuture",
    ],
)

axt_android_aar(
    name = "eventto_aar",
    testonly = 1,
    expected_class_prefixes = [
        "androidx.test.eventto",
    ],
    included_dep = ":eventto",
)

maven_artifact(
    name = "eventto_maven_artifact",
    testonly = 1,
    last_updated = "20240709000000",
    target = ":eventto_aar",
)
