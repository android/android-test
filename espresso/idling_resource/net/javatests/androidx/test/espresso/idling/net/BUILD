# Description:
# Tests for Network Idling Resources

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_library_test(
    name = "UriIdlingResourceTest",
    srcs = ["UriIdlingResourceTest.java"],
    deps = [
        "//espresso/idling_resource/java/androidx/test/espresso:idling_resource_internal",
        "//espresso/idling_resource/net/java/androidx/test/espresso/idling/net:uri_idling_resource",
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_mockito_mockito_core",
    ],
)
