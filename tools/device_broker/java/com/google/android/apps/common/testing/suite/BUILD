# Entry-point test suite used for Android Tests

licenses(["notice"])  # Apache 2.0

ANDROID_TEST_HARNESS_SRCS = [
    "AndroidTestHarness.java",
]

java_library(
    name = "suite",
    testonly = 1,
    srcs = glob(
        ["*.java"],
        exclude = (ANDROID_TEST_HARNESS_SRCS),
    ),
    visibility = ["//visibility:public"],
    deps = [
        ":test_controller",
        "//java/com/google/common/annotations",
        "//java/com/google/common/base",
        "//java/com/google/common/collect",
        "//java/com/google/common/flags",
        "//java/com/google/common/io",
        "//java/com/google/common/math",
        "//java/com/google/common/net",
        "//java/com/google/io/base",
        "//tools/device_broker/java/com/google/android/apps/common/testing/broker:broker_factory",
        "//tools/device_broker/java/com/google/android/apps/common/testing/broker:broker_intf",
        "//tools/device_broker/java/com/google/android/apps/common/testing/broker:shared_constants",
        "//tools/device_broker/java/com/google/android/apps/common/testing/broker:struct",
        "//tools/device_broker/java/com/google/android/apps/common/testing/suite/filter",
        "//tools/device_broker/java/com/google/android/apps/common/testing/testrunner/testsuitepbutil",
        "//tools/device_broker/proto:test_info_pb_java_proto",
        "//third_party/java/jcommander",
        "//third_party/java/jsr305_annotations",
        "//third_party/java/junit",
    ],
)

java_library(
    name = "exceptions",
    testonly = 1,
    srcs = ["SuiteCreationFailureException.java"],
    visibility = ["//visibility:public"],
    deps = ["//third_party/java/jsr305_annotations"],
)

java_test(
    name = "entry_point",
    testonly = 1,
    tags = ["manual"],
    test_class = "com.google.android.apps.common.testing.suite.AndroidDeviceTestSuite",
    visibility = ["//visibility:public"],
    runtime_deps = [
        ":suite",
    ],
)

java_library(
    name = "test_controller",
    testonly = 1,
    srcs = ANDROID_TEST_HARNESS_SRCS,
    visibility = ["//tools/device_broker/javatests:__subpackages__"],
    deps = ["//tools/device_broker/java/com/google/android/apps/common/testing/broker:broker_intf"],
)
