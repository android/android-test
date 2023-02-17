# Publicly visible androidx.test.core API library

package(default_applicable_licenses = ["//:license"])

licenses(["notice"])

alias(
    name = "core",
    testonly = 1,
    actual = "//core/java/androidx/test/core",
    visibility = ["//visibility:public"],
)

# The manifest entries for ActivityScenario's bootstrap activities.
# Add this to to your application under test to avoid a separate test process getting launched
# for the bootstrap activities
alias(
    name = "manifest",
    testonly = 1,
    actual = "//core/java/androidx/test/core:manifest",
    visibility = ["//visibility:public"],
)
