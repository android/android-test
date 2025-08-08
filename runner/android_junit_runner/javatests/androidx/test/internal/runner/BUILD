# Description: Tests for Runner

load("//build_extensions:android_library_test.bzl", "axt_android_library_test")
load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

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
        "@androidsdk//:legacy_test-36",
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
        "@androidsdk//:legacy_test-36",
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
        "@androidsdk//:legacy_test-36",
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
        "@androidsdk//:legacy_test-36",
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
        "@androidsdk//:legacy_test-36",
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
        "@androidsdk//:legacy_test-36",
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
        "@androidsdk//:legacy_test-36",
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
        "@androidsdk//:legacy_test-36",
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
        "@androidsdk//:legacy_test-36",
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
        "@androidsdk//:legacy_test-36",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)
