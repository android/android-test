# Description:
#   Espresso Web Assertion

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

REMOTE_WEB_ASSERTIONS = [
    "RemoteWebViewAssertions.java",
    "CheckResultWebAssertionRemoteMessage.java",
    "DocumentProtoConverters.java",
    "DocumentToByteStringConverter.java",
    "ByteStringToDocumentConverter.java",
    "DocumentToByteStringConverter.java",
    "CompressorDecompressor.java",
]

android_library(
    name = "remote_web_assertions",
    srcs = glob(
        REMOTE_WEB_ASSERTIONS,
    ),
    deps = [
        "//espresso/core/java/androidx/test/espresso/remote:exceptions",
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/remote/java/androidx/test/espresso/remote:protoUtils",
        "//espresso/remote/java/androidx/test/espresso/remote:registry",
        "//espresso/remote/java/androidx/test/espresso/web/proto/assertion:web_assertions_proto_android_pb3",
        "//espresso/web/java/androidx/test/espresso/web/assertion",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "@com_google_protobuf//:protobuf_javalite",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)
