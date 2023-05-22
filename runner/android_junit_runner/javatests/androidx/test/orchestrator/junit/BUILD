load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")

package(default_applicable_licenses = ["//:license"])

# Description: Tests for the Orchestrator client connection
licenses(["notice"])

axt_android_local_test(
    name = "BundleJUnitUtilsTest",
    size = "small",
    srcs = [
        "BundleJUnitUtilsTest.java",
    ],
    deps = [
        "//runner/android_junit_runner",
        "//runner/android_junit_runner/javatests/androidx/test/orchestrator:sample_junit_test",
        "//services/events/java/androidx/test/services/events",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)
