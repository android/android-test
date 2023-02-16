# Description: Monitor

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)
load(
    "//build_extensions:phone_devices.bzl",
    "apis",
    "devices",
)

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
    default_visibility = [
        "//visibility:private",
    ],
)

licenses(["notice"])

# HardwareRendererCompatRenderingDisabledTest needs a separate rule, since it only
# runs on slim image
axt_android_library_test(
    name = "HardwareRendererCompatRenderingDisabledTest",
    srcs = ["HardwareRendererCompatRenderingDisabledTest.java"],
    # only run on slim image
    device_list = devices(
        [30],
        use_slim = True,
    ),
    deps = [
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/monitor/javatests/androidx/test/platform/view/inspector/fixtures",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "HardwareRendererCompatUnsupportedTest",
    srcs = ["HardwareRendererCompatUnsupportedTest.java"],
    device_list = devices(apis(max_api = 29)),
    deps = [
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/monitor/javatests/androidx/test/platform/view/inspector/fixtures",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
