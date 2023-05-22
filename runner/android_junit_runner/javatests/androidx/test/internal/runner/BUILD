# Description: Tests for Runner

load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")
load("//build_extensions:android_library_test.bzl", "axt_android_library_test")
load("//build_extensions:axt_android_application_test.bzl", "axt_android_application_test")
load(
    "//build_extensions:phone_devices.bzl",
    "apis",
    "devices",
)
load("@build_bazel_rules_android//android:rules.bzl", "android_binary")

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

# test classpath scanning for a multidex self-instrumenting apk
axt_android_library_test(
    name = "ClassPathScannerTest",
    srcs = ["ClassPathScannerTest.java"],
    # api 15 is flaky
    # dex2oat fails on 24 + 25 for an unknown reason
    device_list = devices(apis(
        exclude_apis = [
            24,
            25,
        ],
        min_api = 16,
    )),
    deps = [
        # include the large number of methods that will force classes off main dex
        "//testapps/multidex_testapp/java/androidx/test/multidex/app:src",
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
        "@maven//:org_mockito_mockito_core",
        "@maven//:com_google_truth_truth",
    ],
)

# # test classpath scanning for a multidex test apk, with a small app apk
android_binary(
    name = "stub_target_app",
    manifest = "//build_extensions:AndroidManifest_target_stub.xml",
    manifest_values = {
        "applicationId": "androidx.test.multidex",
    },
    multidex = "legacy",
    deps = [
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "@maven//:androidx_multidex_multidex",
    ],
)

axt_android_application_test(
    name = "ClassPathScannerTest_stub_application_under_test",
    srcs = ["ClassPathScannerTest.java"],
    # TODO(b/116007991): enable more APIs once fixed
    device_list = devices([
        21,
        22,
        23,
        26,
        27,
        28,
        29,
        30,
    ]),
    instruments = ":stub_target_app",
    manifest = "//build_extensions:AndroidManifest_instrumentation_test_template.xml",
    manifest_values = {
        "applicationId": "androidx.test.tests",
        "instrumentationTargetPackage": "androidx.test.multidex",
        "minSdkVersion": "21",
    },
    multidex = "legacy",
    deps = [
        # include the large number of methods that will force classes off main dex
        "//testapps/multidex_testapp/java/androidx/test/multidex/app:src",
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:junit_junit",
        "@maven//:org_mockito_mockito_core",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_library_test(
    name = "AndroidLogOnlyBuilderTest",
    srcs = [
        "AndroidLogOnlyBuilderTest.java",
    ],
    deps = [
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

axt_android_library_test(
    name = "AndroidRunnerBuilderTest",
    srcs = [
        "AndroidRunnerBuilderTest.java",
    ],
    deps = [
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
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_library_test(
    name = "DirectTestLoaderTest",
    srcs = [
        "DirectTestLoaderTest.java",
    ],
    # needs uses-library android.test.runner
    manifest = "AndroidManifest.xml",
    deps = [
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

axt_android_library_test(
    name = "ExcludePackageNameFilterTest",
    srcs = [
        "ExcludePackageNameFilterTest.java",
    ],
    deps = [
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

axt_android_library_test(
    name = "ExternalClassNameFilterTest",
    srcs = [
        "ExternalClassNameFilterTest.java",
    ],
    deps = [
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

axt_android_library_test(
    name = "InclusivePackageNamesFilterTest",
    srcs = [
        "InclusivePackageNamesFilterTest.java",
    ],
    deps = [
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

axt_android_local_test(
    name = "RunnerArgsTest",
    srcs = ["RunnerArgsTest.java"],
    deps = [
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "@maven//:com_google_truth_truth",
    ],
)

axt_android_library_test(
    name = "RunnerArgsTest_android",
    srcs = ["RunnerArgsTest.java"],
    shard_count = 4,
    deps = [
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "ScanningTestLoaderTest",
    srcs = [
        "ScanningTestLoaderTest.java",
    ],
    # needs uses-library android.test.runner
    manifest = "AndroidManifest.xml",
    deps = [
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

axt_android_local_test(
    name = "SdkSuppressTest",
    srcs = ["SdkSuppressTest.java"],
    deps = [
        "//runner/android_junit_runner",
        "@maven//:com_google_truth_truth",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)

axt_android_library_test(
    name = "TestExecutorTest",
    srcs = [
        "TestExecutorTest.java",
    ],
    deps = [
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

axt_android_local_test(
    name = "TestRequestBuilderLocalTest",
    srcs = ["TestRequestBuilderLocalTest.java"],
    deps = [
        "//runner/android_junit_runner",
        "@maven//:com_google_truth_truth",
        "@maven//:org_mockito_mockito_core",
    ],
)

axt_android_library_test(
    name = "TestRequestBuilderTest",
    srcs = [
        "TestRequestBuilderTest.java",
    ],
    manifest = "AndroidManifest.xml",
    shard_count = 5,
    deps = [
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

axt_android_library_test(
    name = "TestSizeTest",
    srcs = [
        "TestSizeTest.java",
    ],
    manifest = "AndroidManifest.xml",
    deps = [
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "@androidsdk//:legacy_test-30",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
