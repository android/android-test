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
    name = "AndroidAnnotatedBuilderTest",
    srcs = [
        "AndroidAnnotatedBuilderTest.java",
    ],
    deps = [
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "@maven//:junit_junit",
        "@maven//:org_mockito_mockito_core",
    ],
)
