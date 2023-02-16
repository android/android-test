# Description: Monitor

load(
    "//build_extensions:axt_android_local_test.bzl",
    "axt_android_local_test",
)

package(
    default_testonly = 1,
)

licenses(["notice"])

axt_android_local_test(
    name = "PlatformTestStorageRegistryTest",
    srcs = ["PlatformTestStorageRegistryTest.java"],
    deps = [
        "//core",
        "//ext/junit",
        "//opensource:hamcrest",
        "//runner/android_junit_runner",
        "//runner/monitor",
        "//runner/monitor/javatests/androidx/test/internal/platform:fixtures",
        "//runner/monitor/javatests/androidx/test/platform/view/inspector/fixtures",
        "//runner/rules",
        "//services/storage",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_mockito_mockito_core",
    ],
)
