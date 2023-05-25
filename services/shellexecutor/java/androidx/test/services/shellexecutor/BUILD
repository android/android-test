load("//build_extensions:kt_android_library.bzl", "kt_android_library")

# A shell command execution server to allow shell commands to be run at elevated permissions

package(default_applicable_licenses = ["//services:license"])

licenses(["notice"])

kt_android_library(
    name = "coroutine_file_observer",
    srcs = [
        "CoroutineFileObserver.kt",
    ],
    visibility = ["//visibility:private"],
    deps = [
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_core",
    ],
)

kt_android_library(
    name = "file_observer_protocol",
    srcs = [
        "FileObserverProtocol.kt",
        "Messages.kt",
    ],
    visibility = [
        "//services/shellexecutor/javatests/androidx/test/services/shellexecutor:__subpackages__",
    ],
)

kt_android_library(
    name = "exec_server",
    srcs = [
        "BlockingPublish.java",
        "FileObserverShellMain.kt",
        "ShellCommand.java",
        "ShellCommandExecutor.java",
        "ShellCommandExecutorServer.java",
        "ShellCommandFileObserverExecutorServer.kt",
        "ShellExecSharedConstants.java",
        "ShellMain.java",
    ],
    idl_srcs = ["Command.aidl"],
    visibility = [":export"],
    deps = [
        ":coroutine_file_observer",
        ":file_observer_protocol",
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
        "ShellCommandFileObserverClient.kt",
        "ShellExecSharedConstants.java",
        "ShellExecutor.java",
        "ShellExecutorFactory.java",
        "ShellExecutorFileObserverImpl.kt",
        "ShellExecutorImpl.java",
    ],
    idl_srcs = ["Command.aidl"],
    visibility = [":export"],
    deps = [
        ":coroutine_file_observer",
        ":file_observer_protocol",
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
        ":__pkg__",
    ],
)

# Do not link to these targets directly, rely on exporting target instead
package_group(
    name = "export",
    packages = [
        "//services/shellexecutor",
    ],
)
