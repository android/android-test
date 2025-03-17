load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")

package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
)

# Tests for On-Device Infrastructure - Orchestrator
licenses(["notice"])

axt_android_local_test(
    name = "StackTrimmerTest",
    size = "small",
    srcs = [
        "StackTrimmerTest.java",
    ],
    manifest_values = {
        "minSdkVersion": "21",
    },
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)
