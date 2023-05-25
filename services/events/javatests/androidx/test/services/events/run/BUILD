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
    name = "TestAssumptionFailureEventTest",
    size = "small",
    srcs = [
        "TestAssumptionFailureEventTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "TestFailureEventTest",
    size = "small",
    srcs = [
        "TestFailureEventTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "TestFinishedEventTest",
    size = "small",
    srcs = [
        "TestFinishedEventTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "TestIgnoredEventTest",
    size = "small",
    srcs = [
        "TestIgnoredEventTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

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
    name = "TestStartedEventTest",
    size = "small",
    srcs = [
        "TestStartedEventTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)
