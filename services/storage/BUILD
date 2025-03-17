package(
    default_applicable_licenses = ["//services:license"],
    default_testonly = 1,
)

licenses(["notice"])

# AndroidX Test storage API.
alias(
    name = "storage",
    testonly = 0,
    actual = "//services/storage/java/androidx/test/services/storage",
    visibility = ["//visibility:public"],
)

# Constants shared between on-device (android) and host-side (java code) testing
# infrastructure for the storage service.
alias(
    name = "storage_constants",
    testonly = 0,
    actual = "//services/storage/java/androidx/test/services/storage:test_storage_constants",
    visibility = ["//visibility:public"],
)
