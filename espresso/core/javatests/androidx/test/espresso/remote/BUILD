# Description:
#   Tests for espresso remote classes

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
)

licenses(["notice"])

android_library(
    name = "util",
    srcs = [
        "StubRootMatcher.java",
        "TestTypeRemoteMessage.java",
        "TestTypes.java",
    ],
    visibility = ["//espresso/remote/javatests/androidx/test/espresso:__subpackages__"],
    deps = [
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//espresso/remote/javatests/androidx/test/espresso/proto:test_protos_android_pb3",
        "//espresso/web/java/androidx/test/espresso/web/model",
        "@com_google_protobuf//:protobuf_javalite",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

axt_android_library_test(
    name = "ConstructorInvocationTest",
    srcs =
        ["ConstructorInvocationTest.java"],
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

axt_android_library_test(
    name = "MethodInvocationTest",
    srcs =
        ["MethodInvocationTest.java"],
    deps = [
        ":util",
        "//core",
        "//espresso/core/java/androidx/test/espresso/remote:reflectionUtils",
        "//ext/junit",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
