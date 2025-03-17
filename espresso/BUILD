load("@rules_license//rules:license.bzl", "license")

package(
    default_applicable_licenses = ["//espresso:license"],
    default_testonly = 1,
)

license(
    name = "license",
    package_name = "espresso",
)

licenses(["notice"])

exports_files([
    "proguard_binary.cfg",
    "LICENSE",
])
