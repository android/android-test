load("//build_extensions:android_library_test.bzl", "axt_android_library_test")
load(
    "//build_extensions:phone_devices.bzl",
    "apis",
    "devices",
)

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

axt_android_library_test(
    name = "AtraceLoggerTest",
    srcs = ["AtraceLoggerTest.kt"],
    device_list = devices(
        apis(min_api = 21),
    ),
    deps = [
        "//ext/junit",
        "//runner/monitor",
        "//runner/rules",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
