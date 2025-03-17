load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
)

licenses(["notice"])

android_library(
    name = "connection",
    testonly = 0,
    srcs = ["Connection.java"],
    deps = [":receivers"],
)

android_library(
    name = "tool_connection",
    testonly = 0,
    srcs = ["ToolConnection.java"],
    visibility = [
        "//services:__subpackages__",
    ],
    exports = [
        ":connection",
        ":receivers",
    ],
    deps = [
        ":connection",
        ":receivers",
        "//services/speakeasy/java/androidx/test/services/speakeasy:protocol",
        "//services/speakeasy/java/androidx/test/services/speakeasy/compilehelp/hidden_api:ActivityManagerNative",
    ],
)

android_library(
    name = "receivers",
    testonly = 0,
    srcs = [
        "FindResultReceiver.java",
        "PublishResultReceiver.java",
    ],
    deps = [
        "//services/speakeasy/java/androidx/test/services/speakeasy:protocol",
    ],
)

android_library(
    name = "client",
    testonly = 0,
    srcs = [
        "AppConnection.java",
    ],
    visibility = [
        "//services:__subpackages__",
    ],
    exports = [
        ":connection",
        ":receivers",
    ],
    deps = [
        ":connection",
        ":receivers",
        "//services/speakeasy/java/androidx/test/services/speakeasy:protocol",
    ],
)
