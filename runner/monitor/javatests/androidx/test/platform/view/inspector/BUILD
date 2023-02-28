# Description: Monitor

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
    default_visibility = [
        "//visibility:private",
    ],
)

licenses(["notice"])

axt_android_library_test(
    name = "WindowInspectorCompatTest",
    srcs = ["WindowInspectorCompatTest.kt"],
    deps = [
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/monitor",
        "//runner/monitor/javatests/androidx/test/internal/platform:fixtures",
        "//runner/monitor/javatests/androidx/test/platform/view/inspector/fixtures",
        "//runner/rules",
        "//services/storage",
        "@maven//:androidx_annotation_annotation",
        "@maven//:com_google_guava_guava",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_mockito_mockito_core",
    ],
)
