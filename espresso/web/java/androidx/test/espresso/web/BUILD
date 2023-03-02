load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions/maven:axt_android_aar.bzl", "axt_android_aar")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions:dackka_test.bzl", "dackka_test")
load("//build_extensions:api_checks.bzl", "api_checks")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//:__subpackages__"],
)

android_library(
    name = "espresso_web_aar_lib",
    manifest = "AndroidManifest.xml",
    proguard_specs = [
        ":proguard_library.cfg",
    ],
    exports = [
        "//espresso/web/java/androidx/test/espresso/web/action",
        "//espresso/web/java/androidx/test/espresso/web/assertion",
        "//espresso/web/java/androidx/test/espresso/web/matcher",
        "//espresso/web/java/androidx/test/espresso/web/model",
        "//espresso/web/java/androidx/test/espresso/web/sugar",
        "//espresso/web/java/androidx/test/espresso/web/webdriver",
    ],
)

axt_android_aar(
    name = "espresso_web_aar",
    expected_class_prefixes = [
        "androidx.test.espresso.web",
    ],
    included_dep = ":espresso_web_aar_lib",
)

maven_artifact(
    name = "espresso_web_maven_artifact",
    last_updated = "20170622000000",
    target = ":espresso_web_aar",
)

dackka_test(
    name = "espresso_web_doc",
    runtime_dep = ":espresso_web_aar_lib",
    src_jar = ":espresso_web_aar-src.jar",
)

api_checks(
    name = "espresso_web_api",
    runtime_dep = ":espresso_web_aar_lib",
    src_jar = ":espresso_web_aar-src.jar",
)
