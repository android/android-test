# Publicly visible alias to the runner target

load("@rules_java//java:defs.bzl", "java_library")

package(
    default_applicable_licenses = ["//:license"],
    default_testonly = 1,
)

licenses(["notice"])

alias(
    name = "android_junit_runner",
    actual = "//runner/android_junit_runner/java/androidx/test:runner",
    visibility = ["//visibility:public"],
)
