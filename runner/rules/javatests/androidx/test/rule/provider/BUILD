# Description: Tests for Rules.
load("//build_extensions:android_library_test.bzl", "axt_android_library_test")

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_library_test(
    name = "DelegatingContextTest",
    srcs = ["DelegatingContextTest.java"],
    deps = [
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/rules",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_mockito_mockito_core",
    ],
)
