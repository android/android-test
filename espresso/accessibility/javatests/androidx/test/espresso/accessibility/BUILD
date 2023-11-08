# Description:
#   Tests for espresso accessibility apis

load(
    "//build_extensions:android_library_test.bzl",
    "axt_android_library_test",
)

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

axt_android_library_test(
    name = "AccessibilityChecksIntegrationTest",
    srcs = ["AccessibilityChecksIntegrationTest.java"],
    deps = [
        "//core",
        "//espresso/accessibility/java/androidx/test/espresso/accessibility:accessibility_checks",
        "//espresso/core/java/androidx/test/espresso",
        "//espresso/core/java/androidx/test/espresso:framework",
        "//espresso/core/java/androidx/test/espresso:interface",
        "//ext/junit",
        "//runner/android_junit_runner",
        "//testapps/ui_testapp/java/androidx/test/ui/app:lib_exported",
        "@maven//:com_google_android_apps_common_testing_accessibility_framework_accessibility_test_framework",
        "@maven//:junit_junit",
        "@maven//:org_hamcrest_hamcrest_core",
    ],
)
