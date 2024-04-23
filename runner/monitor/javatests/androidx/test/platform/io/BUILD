# Description: Monitor

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)
load(
    "//build_extensions:axt_android_local_test.bzl",
    "axt_android_local_test",
)

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_local_test(
    name = "PlatformTestStorageRegistryTest",
    srcs = ["PlatformTestStorageRegistryTest.java"],
    deps = [
        "//core",
        "//ext/junit",
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner",
        "//runner/monitor",
        "//runner/monitor/javatests/androidx/test/internal/platform:fixtures",
        "//runner/monitor/javatests/androidx/test/platform/view/inspector/fixtures",
        "//runner/rules",
        "//services/storage",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_mockito_mockito_core",
    ],
)

android_library(
    name = "storage_test_assets",
    assets = glob(["assets/**"]),
    assets_dir = "assets",
    manifest = "AndroidManifest.xml",
)
