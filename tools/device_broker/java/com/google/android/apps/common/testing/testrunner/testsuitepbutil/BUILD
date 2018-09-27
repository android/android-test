# Utility classes for TestSuitePb manipulation

licenses(["notice"])  # Apache 2.0

package(
    default_visibility = ["//visibility:public"],
)

java_library(
    name = "testsuitepbutil",
    testonly = 1,
    srcs = glob(["*.java"]),
    deps = [
        "//java/com/google/common/collect",
        "//java/com/google/common/io",
        "//tools/device_broker/proto:test_info_pb_java_proto",
    ],
)
