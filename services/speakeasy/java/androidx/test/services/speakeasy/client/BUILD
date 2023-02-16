load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(default_applicable_licenses = ["//services:license"])

licenses(["notice"])

android_library(
    name = "connection",
    srcs = ["Connection.java"],
    deps = [":receivers"],
)

android_library(
    name = "tool_connection",
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
