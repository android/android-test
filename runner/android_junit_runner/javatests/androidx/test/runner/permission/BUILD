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
    name = "GrantPermissionCallableTest",
    srcs = ["GrantPermissionCallableTest.java"],
    deps = [
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_library_test(
    name = "PermissionRequesterTest",
    srcs = ["PermissionRequesterTest.java"],
    deps = [
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_library_test(
    name = "UiAutomationShellCommandTest",
    srcs = ["UiAutomationShellCommandTest.java"],
    deps = [
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
