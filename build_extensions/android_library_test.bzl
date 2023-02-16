"""Wrapper around for android_library_test that adds additionsl features."""

load("@build_bazel_rules_android//android:rules.bzl", "android_library")

def axt_android_library_test(
        name,
        args = [],
        srcs = [],
        custom_package = None,
        data = [],
        device_list = None,
        manifest = None,
        multidex = None,
        deps = [],
        **kwargs):
    """Placeholder for future instrumentation test support.

    Currently only generates an android_library

    """
    android_library(
        name = "%s_lib" % name,
        srcs = srcs,
        exports_manifest = True,
        manifest = manifest,
        deps = deps,
        testonly = 1,
    )
