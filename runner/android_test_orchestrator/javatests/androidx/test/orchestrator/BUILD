# Description: Odo tests.

load(
    "//build_extensions:phone_devices.bzl",
    "apis",
    "devices",
)
load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)
load(
    "//build_extensions:axt_android_local_test.bzl",
    "axt_android_local_test",
)

package(default_applicable_licenses = ["//:license"])

licenses(["notice"])

axt_android_local_test(
    name = "AndroidTestOrchestratorTest",
    size = "small",
    srcs = [
        "AndroidTestOrchestratorTest.java",
    ],
    deps = [
        "//core/java/androidx/test/core",
        "//ext/junit",
        "//runner/android_test_orchestrator",
        "@maven//:com_google_guava_guava",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_local_test(
    name = "TestCoverageTest",
    size = "small",
    srcs = [
        "TestCoverageTest.java",
    ],
    deps = [
        "//core/java/androidx/test/core",
        "//ext/junit",
        "//runner/android_test_orchestrator",
        "@maven//:com_google_guava_guava",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_local_test(
    name = "TestRunnableTest",
    size = "small",
    srcs = [
        "TestRunnableTest.java",
    ],
    deps = [
        "//core/java/androidx/test/core",
        "//ext/junit",
        "//runner/android_test_orchestrator",
        "@maven//:com_google_guava_guava",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "RuntimePermissionsIntegrationTest",
    srcs = ["RuntimePermissionsIntegrationTest.java"],
    args = [
        "--grant_runtime_permissions=false",
    ],
    custom_package = "androidx.test.orchestrator.tests",
    device_list = devices(
        apis(min_api = 24),
    ),
    deps = [
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
