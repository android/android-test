# Alias for androidx.test.espresso.device API library

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

# Add packages here to access EspressoDevice APIs.
package_group(
    name = "device_api_allowlist",
    packages = [
        "//...",
    ],
)

android_library(
    name = "device",
    testonly = 1,
    visibility = [
        ":device_api_allowlist",
    ],
    exports = ["//espresso/device/java/androidx/test/espresso/device"],
)
