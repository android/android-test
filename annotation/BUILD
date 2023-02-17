# Publicly visible androidx.test.annotation API library

load("@rules_license//rules:license.bzl", "license")

package(default_applicable_licenses = ["//:license"])

license(
    name = "license",
    package_name = "annotation",
)

licenses(["notice"])

exports_files(["LICENSE"])

alias(
    name = "annotation",
    actual = "//annotation/java/androidx/test/annotation",
    visibility = ["//visibility:public"],
)
