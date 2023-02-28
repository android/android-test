# Description: Monitor

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
    default_visibility = [
        "//visibility:private",
    ],
)

licenses(["notice"])

axt_android_library_test(
    name = "MonitoringInstrumentationTest",
    srcs = ["MonitoringInstrumentationTest.java"],
    deps = [
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/monitor/javatests/androidx/test/platform/view/inspector/fixtures",
        "@maven//:androidx_annotation_annotation",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_mockito_mockito_core",
    ],
)
