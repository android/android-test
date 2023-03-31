load("@build_bazel_rules_android//android:rules.bzl", "android_library")
load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)
load("//build_extensions:java_service_map.bzl", "java_service_map")

package(default_applicable_licenses = ["//:license"])

android_library(
    name = "fixtures",
    srcs = [
        "ServiceFixture.java",
        "ServiceFixtureImpl.java",
    ],
    visibility = ["//visibility:public"],
    deps = [
    ],
)

java_service_map(
    name = "fixture_map",
    providers = {
        "androidx.test.internal.platform.ServiceFixture": [
            "androidx.test.internal.platform.ServiceFixtureImpl",
        ],
    },
)

axt_android_library_test(
    name = "ServiceLoaderWrapperTest",
    srcs = ["ServiceLoaderWrapperTest.java"],
    tags = [
        # "blaze coverage" fails with StrictMode violations.
        "nozapfhahn",
    ],
    deps = [
        ":fixture_map",  # buildcleaner: keep
        ":fixtures",  # buildcleaner: keep
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
