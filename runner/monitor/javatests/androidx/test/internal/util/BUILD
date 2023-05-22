# Description: Monitor

load(
    "//build_extensions:axt_android_local_test.bzl",
    "axt_android_local_test",
)

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_local_test(
    name = "LogUtilsTest",
    srcs = ["LogUtilsTest.java"],
    deps = [
        "//runner/monitor",
        "@maven//:com_google_truth_truth",
    ],
)
