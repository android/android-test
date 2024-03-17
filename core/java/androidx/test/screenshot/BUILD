load("@io_bazel_rules_kotlin//kotlin:android.bzl", "kt_android_library")

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

kt_android_library(
    name = "bitmap_holder",
    testonly = True,
    srcs = ["BitmapHolder.kt"],
    visibility = ["//visibility:public"],
    deps = [
        "//annotation/java/androidx/test/annotation",
        "//core",
        "//testing/screendiffing/proto:proto2_java_proto_lite",
    ],
)
