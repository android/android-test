# Publicly visible alias to the monitor target

load("@rules_java//java:defs.bzl", "java_library")

package(default_applicable_licenses = ["//:license"])

licenses(["notice"])

# This needs to be a java_library instead of an alias, so java_* based rules can reference it
java_library(
    name = "monitor",
    visibility = ["//visibility:public"],
    exports = ["//runner/monitor/java/androidx/test:monitor"],
)
