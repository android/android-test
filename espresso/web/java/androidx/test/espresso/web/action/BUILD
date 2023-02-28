# Description:
#   Espresso Web Actions

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

# to generate EvaluationAtom.java.opensource,
# blaze build //tools/android/webdriver/atom:EvaluationAtom_src
# cp blaze-genfiles/tools/android/webdriver/atom/EvaluationAtom.java third_party/android/androidx_test/espresso/web/java/androidx/test/espresso/web/action/EvaluationAtom.java.opensource

android_library(
    name = "action",
    srcs = glob(
        ["*.java"],
    ),
    custom_package = "androidx.test.espresso.web.action",
    idl_import_root = "idls",
    idl_srcs = glob(["idls/**"]),
    deps = [
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//espresso/core/java/androidx/test/espresso/matcher",
        "//espresso/core/java/androidx/test/espresso/remote:bindable",
        "//espresso/core/java/androidx/test/espresso/remote:interface",
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//espresso/core/java/androidx/test/espresso/util",
        "//espresso/web/java/androidx/test/espresso/web/bridge",
        "//espresso/web/java/androidx/test/espresso/web/model",
        "//espresso/web/java/androidx/test/espresso/web/util/concurrent",
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_concurrent_concurrent_futures",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)
