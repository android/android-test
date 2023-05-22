# Description: Tests for Runner

load(
    "//build_extensions:axt_android_local_test.bzl",
    "axt_android_local_test",
)
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
    name = "AndroidJUnitRunnerTest",
    srcs = ["AndroidJUnitRunnerTest.java"],
    deps = [
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/rules",
        "//services/events/java/androidx/test/services/events",
        "@maven//:junit_junit",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_local_test(
    name = "AndroidJUnit4RoboTest",
    srcs = ["AndroidJUnit4RoboTest.java"],
    deps = [
        "//runner/android_junit_runner",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_local_test(
    name = "TraceRunListenerTest",
    srcs = ["TraceRunListenerTest.java"],
    manifest_values = {
        "minSdkVersion": "28",
        "targetSdkVersion": "28",
    },
    deps = [
        "//runner/android_junit_runner",
        "@maven//:androidx_tracing_tracing",
        "@maven//:com_google_truth_truth",
    ],
)
