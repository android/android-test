# Description:
#  The client side for communicating with espresso-remote

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
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
    testonly = 0,
    srcs = REMOTE_INTERFACES,
    visibility = ["//visibility:public"],
    deps = [
        ":exceptions",
        ":reflectionUtils",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//opensource/androidx:annotation",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

REFLECTION_UTILS = [
    "MethodInvocation.java",
    "ConstructorInvocation.java",
]

android_library(
    name = "reflectionUtils",
    testonly = 0,
    srcs = REFLECTION_UTILS,
    visibility = ["//visibility:public"],
    deps = [
        ":exceptions",
        "//opensource/androidx:annotation",
        "//runner/android_junit_runner",
    ],
)

android_library(
    name = "aidls",
    testonly = 0,
    custom_package = "androidx.test.espresso.remote",
    idl_import_root = "idls",
    idl_srcs = glob(["idls/**"]),
    visibility = ["//visibility:public"],
    deps = [
        "//opensource/androidx:annotation",
    ],
)

REMOTE_EXCEPTIONS = [
    "RemoteEspressoException.java",
    "RemoteProtocolException.java",
    "NoRemoteEspressoInstanceException.java",
]

android_library(
    name = "exceptions",
    testonly = 0,
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
    testonly = 0,
    srcs = BINDABLE,
)
