load("//build_extensions:android_library_test.bzl", "axt_android_library_test")

package(default_applicable_licenses = ["//services:license"])

licenses(["notice"])

axt_android_library_test(
    name = "SpeakEasyTest",
    srcs = ["SpeakEasyTest.java"],
    args = [
        "--install_test_services=True",
    ],
    manifest = "AndroidManifest.xml",
    deps = [
        "//services/speakeasy/java/androidx/test/services/speakeasy:protocol",
        "//services/speakeasy/java/androidx/test/services/speakeasy/server:speak_easy_core",
        "@maven//:com_google_code_findbugs_jsr305",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
