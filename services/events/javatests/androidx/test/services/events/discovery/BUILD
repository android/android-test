load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")

# Tests for On-Device Infrastructure - Orchestrator
licenses(["notice"])

package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
    default_visibility = [
        "//:__subpackages__",
    ],
)

axt_android_local_test(
    name = "TestFoundEventTest",
    size = "small",
    srcs = [
        "TestFoundEventTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "TestDiscoveryStartedEventTest",
    size = "small",
    srcs = [
        "TestDiscoveryStartedEventTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "TestDiscoveryFinishedEventTest",
    size = "small",
    srcs = [
        "TestDiscoveryFinishedEventTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)
