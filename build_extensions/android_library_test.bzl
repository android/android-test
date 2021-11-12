"""Wrappers for android_library_test."""

load("//tools/build_defs/android:rules.bzl", "android_library_test")
load("//tools/build_defs/kotlin:rules.bzl", "kt_android_library")
load(
    "//third_party/android/androidx_test/build_extensions:infer_java_package_name.bzl",
    "infer_java_package_name",
)

def axt_android_library_test(
        name,
        srcs = [],
        manifest = None,
        manifest_values = {},
        custom_package = None,
        deps = [],
        **kwargs):
    """A wrapper around android_library_test that auto-generates a manifest if not provided.

    TODO(b/172615902): look into replacing with a macro that returns a manifest instead. Or remove entirely if
    this functionality is ever added to android_library_test

    Args:
        name: the unique name of the rule
        srcs: the test sources
        manifest: the AndroidManifest label to provide to android_library_test. If not specified, a manifest will be auto generated.
        manifest_values: the dictionary of manifest substitutions to provide to android_library_test
        custom_package: the custom application id to use. If unspecified, the application id will be derived based on current package name
        deps: the dependencies
        **kwargs: args to pass to android_library_test
    """
    if not manifest:
        test_application_id = custom_package if custom_package else infer_java_package_name()
        manifest_values = {
            "applicationId": test_application_id,
            "instrumentationTargetPackage": test_application_id,
        }
        manifest = "//third_party/android/androidx_test/build_extensions:AndroidManifest_instrumentation_test_template.xml"
    if _is_kotlin(srcs):
        kt_android_library(
            name = "%s_kt_lib" % name,
            srcs = srcs,
            exports_manifest = True,
            deps = deps,
            testonly = 1,
        )
        deps = [":%s_kt_lib" % name]
        srcs = []

    android_library_test(
        name = name,
        srcs = srcs,
        manifest = manifest,
        manifest_values = manifest_values,
        deps = deps,
        **kwargs
    )

def _is_kotlin(srcs):
    for s in srcs:
        if s.endswith(".kt"):
            return True
    return False
