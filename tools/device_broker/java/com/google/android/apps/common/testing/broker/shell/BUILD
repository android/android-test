licenses(["notice"])  # Apache 2.0

java_library(
    name = "shell",
    srcs = glob(["*.java"]),
    plugins = ["//java/com/google/devtools/build/buildjar/plugin/annotations:google_internal_checker"],
    visibility = [
        "//java/com/google/android/apps/common/testing/broker:__pkg__",
        "//tools/device_broker/java/com/google/android/apps/common/testing/broker:__pkg__",
        "//tools/device_broker/javatests/com/google/android/apps/common/testing/broker/shell:__pkg__",
    ],
    deps = [
        "//java/com/google/common/annotations",
        "//java/com/google/common/base",
        "//java/com/google/common/cache",
        "//java/com/google/common/collect",
        "//java/com/google/common/escape",
        "//java/com/google/common/eventbus",
        "//java/com/google/common/hash",
        "//java/com/google/common/io",
        "//java/com/google/common/primitives",
        "//java/com/google/common/util/concurrent",
    ],
)
