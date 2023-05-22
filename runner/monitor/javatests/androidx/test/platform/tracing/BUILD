# Description: Monitor

load(
    "//build_extensions:axt_android_local_test.bzl",
    "axt_android_local_test",
)
load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
    default_visibility = [
        "//visibility:private",
    ],
)

licenses(["notice"])

axt_android_local_test(
    name = "AndroidXTracerTest",
    srcs = ["AndroidXTracerTest.java"],
    manifest_values = {
        "minSdkVersion": "28",
        "targetSdkVersion": "28",
    },
    deps = [
        "//runner/monitor",
        "@maven//:androidx_tracing_tracing",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_library_test(
    name = "TracingTest",
    srcs =
        [
            "TracingTest.java",
        ],
    deps = [
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/monitor/javatests/androidx/test/platform/view/inspector/fixtures",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
