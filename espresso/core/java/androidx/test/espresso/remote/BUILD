# Description:
#  The client side for communicating with espresso-remote

load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load("//build_extensions:combine_jars.bzl", "combine_jars")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//:__subpackages__"],
)

COMMON_JAVACOPTS = [
    "-Xep:ObjectToString:OFF",  # See http://go/objecttostring-lsc
]

REMOTE_INTERFACES = [
    "NoopRemoteInteraction.java",
    "RemoteInteraction.java",
    "RemoteInteractionRegistry.java",
    "EspressoRemoteMessage.java",
]

android_library(
    name = "interface",
    srcs = REMOTE_INTERFACES,
    visibility = ["//visibility:public"],
    deps = [
        ":exceptions",
        ":reflectionUtils",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//opensource:hamcrest",
        "@maven//:androidx_annotation_annotation",
    ],
)

REFLECTION_UTILS = [
    "MethodInvocation.java",
    "ConstructorInvocation.java",
]

android_library(
    name = "reflectionUtils",
    srcs = REFLECTION_UTILS,
    visibility = ["//visibility:public"],
    deps = [
        ":exceptions",
        "//runner/android_junit_runner",
        "@maven//:androidx_annotation_annotation",
    ],
)

android_library(
    name = "aidls",
    custom_package = "androidx.test.espresso.remote",
    idl_import_root = "idls",
    idl_srcs = glob(["idls/**"]),
    visibility = ["//visibility:public"],
)

REMOTE_EXCEPTIONS = [
    "RemoteEspressoException.java",
    "RemoteProtocolException.java",
    "NoRemoteEspressoInstanceException.java",
]

android_library(
    name = "exceptions",
    srcs = REMOTE_EXCEPTIONS,
    visibility = ["//visibility:public"],
    deps = [
        "//espresso/core/java/androidx/test/espresso:interface",
    ],
)

BINDABLE = [
    "Bindable.java",
]

android_library(
    name = "bindable",
    srcs = BINDABLE,
)

filegroup(
    name = "espresso_remote_src",
    srcs = [
        ":libbindable-src.jar",
        ":libexceptions-src.jar",
        ":libinterface-src.jar",
        ":libreflectionUtils-src.jar",
    ],
)

combine_jars(
    name = "espresso_remote_src_jars",
    srcs = [
        ":espresso_remote_src",
    ],
)
