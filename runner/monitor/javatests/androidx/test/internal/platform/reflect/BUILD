load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

axt_android_library_test(
    name = "ReflectiveFieldTest",
    srcs = ["ReflectiveFieldTest.java"],
    deps = [
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/monitor/javatests/androidx/test/platform/view/inspector/fixtures",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "ReflectiveMethodTest",
    srcs = ["ReflectiveMethodTest.java"],
    deps = [
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/monitor/javatests/androidx/test/platform/view/inspector/fixtures",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
