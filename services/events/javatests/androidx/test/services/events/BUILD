load("//build_extensions:android_library_test.bzl", "axt_android_library_test")
load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")

# Tests for On-Device Infrastructure - Orchestrator
licenses(["notice"])

package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
)

axt_android_local_test(
    name = "TestCaseInfoTest",
    size = "small",
    srcs = [
        "TestCaseInfoTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "FailureInfoTest",
    size = "small",
    srcs = [
        "FailureInfoTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "ParcelableConverterTest",
    size = "small",
    srcs = [
        "ParcelableConverterTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "TestRunInfoTest",
    size = "small",
    srcs = [
        "TestRunInfoTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "ErrorInfoTest",
    size = "small",
    srcs = [
        "ErrorInfoTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "TimeStampTest",
    size = "small",
    srcs = [
        "TimeStampTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_library_test(
    name = "TimeStampEmulatorTest",
    size = "medium",  # generic_phone_android_28_x86 is flaky at "small"
    srcs = ["TimeStampEmulatorTest.java"],
    deps = [
        "//ext/junit",
        "//runner/android_junit_runner",  # unuseddeps: keep
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

axt_android_local_test(
    name = "TestStatusTest",
    size = "small",
    srcs = [
        "TestStatusTest.java",
    ],
    deps = [
        "//ext/junit",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
    ],
)
