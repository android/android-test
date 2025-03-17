# Tests for espresso.internal.data
load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
)

licenses(["notice"])

axt_android_library_test(
    name = "TestFlowVisualizerTest",
    srcs = [
        "TestFlowVisualizerTest.java",
    ],
    deps = [
        "//core/java/androidx/test/core",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso/internal/data",
        "//espresso/core/java/androidx/test/espresso/internal/data/model",
        "//ext/junit",
        "//runner/monitor",
        "//services/storage/java/androidx/test/services/storage",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_mockito_mockito_core",
    ],
)
