# Description:
#   Tests for espresso idling resources

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

axt_android_library_test(
    name = "IdlingRegistryTest",
    srcs = ["IdlingRegistryTest.java"],
    deps = [
        "//core",
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
        "@maven//:org_mockito_mockito_core",
    ],
)
