# Publicly visible androidx.test.annotation API library

load("@rules_license//rules:license.bzl", "license")
load("@build_bazel_rules_android//android:rules.bzl", "android_library")

package(default_applicable_licenses = ["//:license"])

license(
    name = "license",
    package_name = "annotation",
)

licenses(["notice"])

exports_files(["LICENSE"])

android_library(
    name = "annotation",
    visibility = ["//visibility:public"],
    exports = ["//annotation/java/androidx/test/annotation"],
)
