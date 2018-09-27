licenses(["notice"])  # Apache 2.0

java_library(
    name = "dex",
    srcs = glob(["*.java"]),
    visibility = [
        "//java/com/google/android/apps/common/testing/suite/dex:__pkg__",
        "//java/com/google/wireless/qa/mobileharness/shared:__subpackages__",
        "//tools/device_broker/java/com/google/android/apps/common/testing:__subpackages__",
    ],
    deps = [
        "//java/com/google/common/base",
        "//java/com/google/common/collect",
        "//java/com/google/common/util/concurrent",
        "//tools/device_broker/proto:test_info_pb_java_proto",
        "//third_party/java/auto:auto_value",
        "//third_party/java/jsr305_annotations",
    ],
)
