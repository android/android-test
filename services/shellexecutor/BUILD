# A shell command execution server to allow shell commands to be run at elevated permissions

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(default_applicable_licenses = ["//services:license"])

licenses(["notice"])

android_library(
    name = "exec_server",
    visibility = [":server_access"],
    exports = ["//services/shellexecutor/java/androidx/test/services/shellexecutor:exec_server"],
)

alias(
    name = "exec_client",
    actual = "//services/shellexecutor/java/androidx/test/services/shellexecutor:exec_client",
    visibility = ["//visibility:public"],
)

# List any packages allowed to access to server exec server.
package_group(
    name = "server_access",
    packages = [
        "//services",
        "//services/shellexecutor/javatests/androidx/test/services/shellexecutor",
    ],
)

filegroup(
    name = "shellexecuter_src",
    srcs = [
        "//services/shellexecutor/java/androidx/test/services/shellexecutor:libexec_client-src.jar",
        "//services/shellexecutor/java/androidx/test/services/shellexecutor:libexec_server-src.jar",
    ],
    visibility = [":server_access"],
)
