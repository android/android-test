# Description: Tests for Runner

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_library_test(
    name = "BasicScreenCaptureProcessorTest",
    srcs = ["BasicScreenCaptureProcessorTest.java"],
    deps = [
        "//core/java/androidx/test/core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "ScreenCaptureTest",
    srcs = ["ScreenCaptureTest.java"],
    deps = [
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_library_test(
    name = "ScreenshotTest",
    srcs = ["ScreenshotTest.java"],
    deps = [
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_library_test(
    name = "TakeScreenshotCallableTest",
    srcs = ["TakeScreenshotCallableTest.java"],
    deps = [
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
        "@maven//:org_mockito_mockito_core",
    ],
)
