load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:android_library_test.bzl", "axt_android_library_test")
load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")

package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_local_test(
    name = "ShellCommandClientTest",
    size = "small",
    srcs = ["ShellCommandClientTest.java"],
    deps = [
        "//runner/android_junit_runner",
        "//services/shellexecutor:exec_client",
        "//services/shellexecutor:exec_server",
        "@maven//:org_mockito_mockito_core",
    ],
)

android_library(
    name = "dummy_activity",
    srcs = ["DummyActivity.java"],
    deps = [],
)

axt_android_library_test(
    name = "ShellCommandTest",
    srcs = [
        "ShellCommandTest.java",
    ],
    manifest = "AndroidManifest.xml",
    deps = [
        ":dummy_activity",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//services/shellexecutor:exec_client",
        "//services/shellexecutor:exec_server",
        "//services/shellexecutor/java/androidx/test/services/shellexecutor:local_socket_protocol",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "ShellCommandExecutorTest",
    srcs = [
        "ShellCommandExecutorTest.java",
    ],
    manifest = "AndroidManifest.xml",
    deps = [
        ":dummy_activity",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//services/shellexecutor:exec_client",
        "//services/shellexecutor:exec_server",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

axt_android_library_test(
    name = "ShellCommandLocalSocketClientTest",
    srcs = [
        "ShellCommandLocalSocketClientTest.kt",
    ],
    deps = [
        "//runner/monitor",
        "//services/shellexecutor:exec_client",
        "//services/shellexecutor/java/androidx/test/services/shellexecutor:local_socket_protocol",
        "//services/shellexecutor/java/androidx/test/services/shellexecutor:local_socket_protocol_pb_java_proto_lite",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_android",
    ],
)

axt_android_library_test(
    name = "ShellCommandLocalSocketExecutorServerTest",
    srcs = [
        "ShellCommandLocalSocketExecutorServerTest.kt",
    ],
    deps = [
        "//runner/monitor",
        "//services/shellexecutor:exec_server",
        "//services/shellexecutor/java/androidx/test/services/shellexecutor:local_socket_protocol",
        "//services/shellexecutor/java/androidx/test/services/shellexecutor:local_socket_protocol_pb_java_proto_lite",
        "@com_google_protobuf//:protobuf_javalite",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_jetbrains_kotlinx_kotlinx_coroutines_android",
    ],
)

axt_android_library_test(
    name = "ShellExecutorTest",
    srcs = [
        "ShellExecutorTest.java",
    ],
    manifest = "AndroidManifest.xml",
    deps = [
        ":dummy_activity",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//services/shellexecutor:exec_client",
        "//services/shellexecutor:exec_server",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
