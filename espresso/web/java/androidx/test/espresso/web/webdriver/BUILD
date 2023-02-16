# Description:
#   Exposes webdriver automation js fragments as atoms.

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

licenses(["notice"])

package(
    default_applicable_licenses = ["//espresso:license"],
    default_visibility = ["//visibility:public"],
)

# To generate WebDriverAtomScripts.java.opensource:
# $ blaze build blaze-genfiles/tools/android/webdriver/atom/WebDriverAtomScripts.java
# $ cp blaze-genfiles/tools/android/webdriver/atom/WebDriverAtomScripts.java third_party/android/androidx_test/espresso/web/java/androidx/test/espresso/web/webdriver/WebDriverAtomScripts.java.opensource

android_library(
    name = "webdriver",
    srcs = glob(
        ["*java"],
    ),
    deps = [
        "//espresso/core/java/androidx/test/espresso/remote/annotation:remote_msg_annotations",
        "//espresso/web/java/androidx/test/espresso/web/model",
        "//runner/monitor",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)
