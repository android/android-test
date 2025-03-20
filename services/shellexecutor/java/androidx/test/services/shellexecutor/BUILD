load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("@com_google_protobuf//bazel:java_lite_proto_library.bzl", "java_lite_proto_library")
load("@com_google_protobuf//bazel:proto_library.bzl", "proto_library")
load("@rules_java//java:defs.bzl", "java_library")
load("//build_extensions:kt_android_library.bzl", "kt_android_library")

# A shell command execution server to allow shell commands to be run at elevated permissions

package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
)

licenses(["notice"])

proto_library(
    name = "local_socket_protocol_pb",
    srcs = ["local_socket_protocol.proto"],
)

java_lite_proto_library(
    name = "local_socket_protocol_pb_java_proto_lite",
    visibility = [
        "//services/shellexecutor/javatests/androidx/test/services/shellexecutor:__subpackages__",
    ],
    deps = [":local_socket_protocol_pb"],
)

kt_android_library(
    name = "local_socket_protocol",
    srcs = ["LocalSocketProtocol.kt"],
    visibility = [
        "//services/shellexecutor/javatests/androidx/test/services/shellexecutor:__subpackages__",
    ],
    deps = [
        ":local_socket_protocol_pb_java_proto_lite",
        "@com_google_protobuf//:protobuf_javalite",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)

android_library(
    name = "exec_idl",
    idl_srcs = ["Command.aidl"],
    visibility = ["//visibility:private"],
)

kt_android_library(
    name = "exec_server",
    srcs = [
        "BlockingPublish.java",
        "LocalSocketShellMain.kt",
        "ShellCommand.java",
        "ShellCommandExecutor.java",
        "ShellCommandExecutorServer.java",
        "ShellCommandLocalSocketExecutorServer.kt",
        "ShellExecSharedConstants.java",
        "ShellMain.java",
    ],
    visibility = [":export"],
    deps = [
        ":exec_idl",
        ":local_socket_protocol",
        ":local_socket_protocol_pb_java_proto_lite",
        "//services/speakeasy/java/androidx/test/services/speakeasy:protocol",
        "//services/speakeasy/java/androidx/test/services/speakeasy/client",
        "//services/speakeasy/java/androidx/test/services/speakeasy/client:tool_connection",
        "@maven//:com_google_guava_guava",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)

kt_android_library(
    name = "exec_client",
    srcs = [
        "BlockingFind.java",
        "ClientNotConnected.java",
        "ShellCommand.java",
        "ShellCommandClient.java",
        "ShellCommandLocalSocketClient.kt",
        "ShellExecSharedConstants.java",
        "ShellExecutor.java",
        "ShellExecutorFactory.java",
        "ShellExecutorImpl.java",
        "ShellExecutorLocalSocketImpl.kt",
    ],
    visibility = [":export"],
    deps = [
        ":exec_idl",
        ":local_socket_protocol",
        ":local_socket_protocol_pb_java_proto_lite",
        "//services/speakeasy/java/androidx/test/services/speakeasy:protocol",
        "//services/speakeasy/java/androidx/test/services/speakeasy/client",
        "//services/speakeasy/java/androidx/test/services/speakeasy/client:tool_connection",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)

# Make it easy to deserialize exceptions that are sent over from device to host.
java_library(
    name = "exceptions",
    srcs = [
        "ClientNotConnected.java",
    ],
    visibility = [
    ],
)

# Do not link to these targets directly, rely on exporting target instead
package_group(
    name = "export",
    packages = [
        "//services/shellexecutor",
    ],
)
