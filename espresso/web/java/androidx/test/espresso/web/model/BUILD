# Description:
#   Encoder/Decoders for WebView / JSON.
#   Atom interface for wrapping javascripts.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

licenses(["notice"])

android_library(
    name = "model",
    srcs = glob(
        ["*.java"],
    ),
    custom_package = "androidx.test.espresso.web.model",
    idl_import_root = "idls",
    idl_parcelables = glob(["idls/**"]),
    deps = [
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//opensource/androidx:annotation",
        "//runner/monitor",
    ],
)
