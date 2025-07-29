# Description:
#  Annotations for espresso remote.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
)

REMOTE_MSG_ANNOTATIONS = [
    "RemoteMsgField.java",
    "RemoteMsgConstructor.java",
]

android_library(
    name = "remote_msg_annotations",
    srcs = REMOTE_MSG_ANNOTATIONS,
    visibility = ["//visibility:public"],
)
