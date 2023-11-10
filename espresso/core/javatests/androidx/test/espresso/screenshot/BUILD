# Description:
#   Tests for espresso remote classes

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

axt_android_library_test(
    name = "ViewInteractionCaptureJavaTest",
    srcs =
        ["ViewInteractionCaptureJavaTest.java"],
    deps = [
        "//core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:espresso_compiletime",
        "//ext/junit",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
