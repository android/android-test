"""Common functions for androidx_test tests."""

load("@build_bazel_rules_android//android:rules.bzl", "android_test")

API_LEVELS = [
    "15_x86",
    "16_x86",
    "17_x86",
    "18_x86",
    "19_x86",
    "21_x86",
    "22_x86",
    "23_x86",
    "24_x86",
    "25_x86",
]

def axt_test(src, deps, extra_args = [], shard_count = 1, size = "medium", timeout = "moderate"):
    # assume src has .java suffix
    name_prefix = src[:-5]
    for api in API_LEVELS:
        axt_test_target(name_prefix, src, api, deps, extra_args, shard_count, size, timeout)

def axt_test_target(
        name_prefix,
        src,
        api_level,
        deps,
        extra_args = [],
        shard_count = 1,
        size = "medium",
        timeout = "moderate",
        extra_tags = []):
    android_test(
        name = "%s_%s" % (name_prefix, api_level),
        size = size,
        timeout = timeout,
        srcs = [src],
        args = [
            "--clear_package_data",
            "--test_filter_spec=-TEST_NAME",
            "--test_package_names=androidx.test.testing.fixtures",
            "--install_test_services=True",
        ] + extra_args,
        manifest = "AndroidManifest.xml",
        shard_count = shard_count,
        tags = extra_tags,
        target_devices = ["//tools/android/emulated_devices/generic_phone:android_%s" % (api_level)],
        deps = deps,
    )
