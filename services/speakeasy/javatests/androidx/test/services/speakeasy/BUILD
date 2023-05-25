load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")

package(default_applicable_licenses = ["//services:license"])

licenses(["notice"])

axt_android_local_test(
    name = "SpeakEasyProtocolTest",
    size = "small",
    srcs = ["SpeakEasyProtocolTest.java"],
    deps = [
        "//services/speakeasy/java/androidx/test/services/speakeasy:protocol",
        "@maven//:com_google_truth_truth",
    ],
)
