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

TEST_DEPS = [
    "//ext/junit",
    "//services/events/java/androidx/test/services/events",
    "@maven//:junit_junit",
    "@maven//:com_google_truth_truth",
]

axt_android_local_test(
    name = "TestRunFinishedEventTest",
    size = "small",
    srcs = [
        "TestRunFinishedEventTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "TestRunErrorEventTest",
    size = "small",
    srcs = [
        "TestRunErrorEventTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "TestRunStartedEventTest",
    size = "small",
    srcs = [
        "TestRunStartedEventTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "TestCaseStartedEventTest",
    size = "small",
    srcs = [
        "TestCaseStartedEventTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "TestCaseErrorEventTest",
    size = "small",
    srcs = [
        "TestCaseErrorEventTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "TestCaseFinishedEventTest",
    size = "small",
    srcs = [
        "TestCaseFinishedEventTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)
