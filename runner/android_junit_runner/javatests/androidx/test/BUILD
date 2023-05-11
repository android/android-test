# Description: Tests for Runner

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)
load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

android_library(
    name = "legacy_test_manifest",
    exports_manifest = 1,
    manifest = "AndroidManifest.xml",
)

axt_android_library_test(
    name = "FailureAssumptionTest",
    srcs = ["FailureAssumptionTest.java"],
    deps = [
        ":legacy_test_manifest",
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "JUnit4CategoryTest",
    srcs = ["JUnit4CategoryTest.java"],
    deps = [
        ":legacy_test_manifest",
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

axt_android_library_test(
    name = "MyAndroidTestCase",
    srcs = ["MyAndroidTestCase.java"],
    deps = [
        ":legacy_test_manifest",
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "@androidsdk//:legacy_test-30",
    ],
)

axt_android_library_test(
    name = "MyInstrumentationTestCase",
    srcs = ["MyInstrumentationTestCase.java"],
    deps = [
        ":legacy_test_manifest",
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "@androidsdk//:legacy_test-30",
    ],
)

axt_android_library_test(
    name = "MyParameterizedTest",
    srcs = ["MyParameterizedTest.java"],
    deps = [
        ":legacy_test_manifest",
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "ParameterizedWithCommaTest",
    srcs = ["ParameterizedWithCommaTest.java"],
    deps = [
        ":legacy_test_manifest",
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "TheoriesAndTestsTogether",
    srcs = ["TheoriesAndTestsTogether.java"],
    deps = [
        ":legacy_test_manifest",
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)

axt_android_library_test(
    name = "TimeoutTest",
    srcs = ["TimeoutTest.java"],
    deps = [
        ":legacy_test_manifest",
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "//runner/rules",
        "//services/events/java/androidx/test/services/events",
        "//services/storage",
        "@androidsdk//:legacy_test-30",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_mockito_mockito_core",
    ],
)

# intentionally omitted tests
# suites not supported in Google3
#"JUnitTestSuiteTest.java",
#"MyTestSuiteBuilder.java",
# performance/profiling test
#"JUnit3HogTest.java",
#"JUnit4HogTest.java",
# empty tests not supported
#"MyEmptyTest.java",

# A basic test to use as baseline benchmark
axt_android_library_test(
    name = "SimpleTest",
    srcs = [
        "SimpleTest.java",
    ],
    deps = [
        "//ext/junit",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
