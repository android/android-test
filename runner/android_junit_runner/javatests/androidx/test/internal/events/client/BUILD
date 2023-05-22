load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load(
    "//build_extensions:axt_android_local_test.bzl",
    "axt_android_local_test",
)

package(default_applicable_licenses = ["//:license"])

# Description: Tests for the Orchestrator client connection
licenses(["notice"])

axt_android_local_test(
    name = "TestEventServiceConnectionBaseTest",
    size = "small",
    srcs = [
        "TestEventServiceConnectionBaseTest.java",
    ],
    deps = [
        "//runner/android_junit_runner",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

android_library(
    name = "sampletest",
    srcs = ["SampleJUnitTest.java"],
    deps = ["@maven//:junit_junit"],
)

axt_android_local_test(
    name = "OrchestratedInstrumentationListenerTest",
    size = "small",
    srcs = [
        "OrchestratedInstrumentationListenerTest.java",
    ],
    deps = [
        ":sampletest",
        "//runner/android_junit_runner",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_local_test(
    name = "TestDiscoveryListenerTest",
    size = "small",
    srcs = [
        "TestDiscoveryListenerTest.java",
    ],
    deps = [
        "//ext/junit",
        "//runner/android_junit_runner",
        "//services/events/java/androidx/test/services/events",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_local_test(
    name = "TestEventClientArgsTest",
    size = "small",
    srcs = [
        "TestEventClientArgsTest.java",
    ],
    deps = [
        "//runner/android_junit_runner",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_local_test(
    name = "TestEventClientTest",
    size = "small",
    srcs = [
        "TestEventClientTest.java",
    ],
    deps = [
        "//runner/android_junit_runner",
        "//services/events/java/androidx/test/services/events",
        "@maven//:androidx_annotation_annotation",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_local_test(
    name = "TestPlatformListenerTest",
    size = "small",
    srcs = [
        "TestPlatformListenerTest.java",
    ],
    deps = [
        "//runner/android_junit_runner",
        "//services/events/java/androidx/test/services/events",
        "@maven//:com_google_truth_truth",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_local_test(
    name = "JUnitDescriptionParserTest",
    size = "small",
    srcs = ["JUnitDescriptionParserTest.java"],
    deps = [
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:com_google_truth_truth",
    ],
)
