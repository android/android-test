# Description: Odo tests.

load(
    "//build_extensions:axt_android_local_test.bzl",
    "axt_android_local_test",
)

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

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
        "@maven//:com_google_truth_truth",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
