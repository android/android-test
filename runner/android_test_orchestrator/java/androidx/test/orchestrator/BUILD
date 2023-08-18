# Description:
#   On Device Orchestrator, to run instrumentation tests sequentially from a single adb command

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//:license"],
    default_visibility = ["//:__subpackages__"],
)

android_library(
    name = "orchestrator",
    srcs = glob(["**/*java"]),
    idl_srcs = glob(["**/*.aidl"]),
    visibility = [
        ":export",
    ],
    deps = [
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner",
        "//services/shellexecutor:exec_client",
        "@maven//:androidx_core_core",
        "@maven//:com_google_guava_guava",
    ],
)

# Do not link to these targets directly, rely on exporting target instead
package_group(
    name = "export",
    packages = [
        "//runner/android_test_orchestrator",
        "//runner/android_test_orchestrator/stubapp",
    ],
)

filegroup(
    name = "orchestrator_src",
    srcs = [
        ":liborchestrator-src.jar",
    ],
)
