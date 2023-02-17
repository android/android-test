# Alias for androidx.test.espresso.device API library

package(default_applicable_licenses = ["//espresso:license"])

licenses(["notice"])

# Add packages here to access EspressoDevice APIs.
package_group(
    name = "device_api_allowlist",
    packages = [
        "//...",
    ],
)

alias(
    name = "device",
    testonly = 1,
    actual = "//espresso/device/java/androidx/test/espresso/device",
    visibility = [
        ":device_api_allowlist",
    ],
)
