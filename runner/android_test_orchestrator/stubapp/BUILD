load("@build_bazel_rules_android//android:rules.bzl", "android_binary")
load("//build_extensions/maven:maven_artifact.bzl", "maven_artifact")
load("//build_extensions/maven:axt_maven_apk.bzl", "axt_maven_apk")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_visibility = ["//:__subpackages__"],
)

# Dummy app to allow the On Device Orchestration to run and execute.
android_binary(
    name = "stubapp",
    srcs = ["src/androidx/test/orchestrator/Orchestrator.java"],
    custom_package = "androidx.test.orchestrator.stubapp",
    manifest = "src/AndroidManifest.xml",
    multidex = "native",  # actually fits into one file, so skip main dex list
    proguard_specs = [
        "//:proguard_binary.cfg",
        "proguard_binary.cfg",
    ],
    visibility = ["//visibility:public"],
    deps = [
        "//runner/android_junit_runner",
        "//runner/android_test_orchestrator",
        "@maven//:androidx_multidex_multidex",
        "@maven//:com_google_guava_guava",
        "@maven//:junit_junit",
    ],
)

axt_maven_apk(
    name = "orchestrator_release_apk",
    included_dep = ":stubapp",
)

maven_artifact(
    name = "orchestrator_release_maven_artifact",
    last_updated = "20170622000000",
    target = ":orchestrator_release_apk",
)
