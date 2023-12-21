# Description:
#   Tests for espresso util classes

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)
load("//build_extensions:axt_android_local_test.bzl", "axt_android_local_test")

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

axt_android_library_test(
    name = "HumanReadablesTest_android",
    srcs = ["HumanReadablesTest.java"],
    deps = [
        "//espresso/core/java/androidx/test/espresso/util",
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_library_test(
    name = "TreeIterablesTest",
    srcs = ["TreeIterablesTest.java"],
    deps = [
        "//espresso/core/java/androidx/test/espresso/util",
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
        "@maven//:org_jetbrains_kotlin_kotlin_stdlib",
    ],
)

axt_android_library_test(
    name = "TracingUtilTest",
    srcs = ["TracingUtilTest.java"],
    deps = [
        "//espresso/core/java/androidx/test/espresso/util",
        "//ext/junit",
        "//runner/android_junit_runner",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
        "@maven//:org_hamcrest_hamcrest_library",
    ],
)

axt_android_local_test(
    name = "HumanReadablesTest",
    srcs = ["HumanReadablesTest.java"],
    deps = [
        "//espresso/core/java/androidx/test/espresso/util",
        "//runner/android_junit_runner",
        "@maven//:com_google_truth_truth",
    ],
)
