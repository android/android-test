load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")

package(default_applicable_licenses = ["//:license"])

# Description: Tests for the Orchestrator client connection
licenses(["notice"])

axt_android_local_test(
    name = "BundleConverterTest",
    size = "small",
    srcs = [
        "BundleConverterTest.java",
    ],
    deps = [
        "//runner/android_junit_runner",
        "//services/events/java/androidx/test/services/events",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_local_test(
    name = "OrchestratorV1ConnectionTest",
    size = "small",
    srcs = [
        "OrchestratorV1ConnectionTest.java",
    ],
    deps = [
        "//runner/android_junit_runner",
        "//services/events/java/androidx/test/services/events",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)
