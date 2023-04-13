# Description:
#   Tests for espresso remote classes

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load(
    "//build_extensions:android_app_instrumentation_tests.bzl",
    "android_app_instrumentation_tests",
)

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

UTIL_SRCS = [
    "StubRootMatcher.java",
    "TestTypes.java",
    "TestTypeRemoteMessage.java",
]

android_library(
    name = "util",
    testonly = 1,
    srcs = UTIL_SRCS,
    visibility = ["//espresso/remote/javatests/androidx/test/espresso:__subpackages__"],
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//espresso/remote/javatests/androidx/test/espresso/proto:test_protos_android_pb3",
        "//espresso/web/java/androidx/test/espresso/web/model",
        "@com_google_protobuf//:protobuf_javalite",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)

android_app_instrumentation_tests(
    name = "instrumentation_tests",
    srcs = glob(
        ["*.java"],
        exclude = UTIL_SRCS,
    ),
    binary_target = "//testapps/ui_testapp/java/androidx/test/ui/app:testapp",
    deps = [
        ":util",
        "//core",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/action",
        "//espresso/core/java/androidx/test/espresso/assertion",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/remote:bindable",
        "//espresso/core/java/androidx/test/espresso/remote:exceptions",
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/core/java/androidx/test/espresso/remote:reflectionUtils",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//ext/junit",
        "//opensource/proto:any_java_proto_lite",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "@com_google_protobuf//:protobuf_javalite",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_mockito_mockito_core",
    ],
)
