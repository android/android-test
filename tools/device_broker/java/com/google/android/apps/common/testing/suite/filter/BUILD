# Classes related to filter test cases to run.

licenses(["notice"])  # Apache 2.0

java_library(
    name = "filter",
    testonly = 1,
    srcs = glob(["*.java"]),
    visibility = [
        "//java/com/google/android/apps/common/testing:testing_pkg",
        "//tools/device_broker/java/com/google/android/apps/common/testing:__subpackages__",
    ],
    deps = [
        "//java/com/google/common/annotations",
        "//java/com/google/common/base",
        "//java/com/google/common/collect",
        "//tools/device_broker/java/com/google/android/apps/common/testing/broker:broker_intf",
        "//tools/device_broker/java/com/google/android/apps/common/testing/suite:exceptions",
        "//tools/device_broker/proto:test_info_pb_java_proto",
        "//third_party/java/jcommander",
        "//third_party/java/junit",
    ],
)
