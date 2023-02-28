# Description: Tests for Runner

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_library_test(
    name = "ActivityLifecycleMonitorImplTest",
    srcs = [
        "ActivityLifecycleMonitorImplTest.java",
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
    name = "ApplicationLifecycleMonitorTest",
    srcs = [
        "ApplicationLifecycleMonitorTest.java",
    ],
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
