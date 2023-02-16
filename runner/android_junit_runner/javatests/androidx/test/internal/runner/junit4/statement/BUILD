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
    name = "ui_thread_statement_test_examples",
    srcs = [
        "UiThreadTestInterface.java",
        "UiThreadTestOnClass.java",
        "UiThreadTestOnImplementedInterface.java",
        "UiThreadTestOnMethod.java",
        "UiThreadTestOnSuperclass.java",
    ],
    deps = [
        "//ext/junit/java/androidx/test/ext/junit",
        "//runner/android_junit_runner/java/androidx/test:runner",
        "//runner/rules",
        "@maven//:junit_junit",
    ],
)

axt_android_library_test(
    name = "UiThreadStatementTest",
    srcs = [
        "UiThreadStatementTest.java",
    ],
    deps = [
        ":ui_thread_statement_test_examples",
        "//core",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//runner/android_junit_runner/javatests/androidx/test/testing/fixtures",
        "@maven//:com_google_truth_truth",
        "@maven//:junit_junit",
    ],
)
