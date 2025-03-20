"""Wrapper around for android_library_test that adds additionsl features."""

load("//build_extensions:kt_android_library.bzl", "kt_android_library")

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

    # always define a manifest to work around 'manifest is required when resource_files or assets are defined.' inherent
    # kt_android_library
    if not manifest:
        manifest = "//build_extensions:AndroidManifest_instrumentation_test_template.xml"

    kt_android_library(
        name = "%s_lib" % name,
        srcs = srcs,
        exports_manifest = True,
        manifest = manifest,
        deps = deps,
        testonly = 1,
    )
