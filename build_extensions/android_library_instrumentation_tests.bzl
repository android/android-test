"""A rule wrapper for an instrumentation test for an android library."""

load(
    "//build_extensions:generate_instrumentation_tests.bzl",
    "generate_instrumentation_tests",
)
load(
    "//build_extensions:infer_android_package_name.bzl",
    "infer_android_package_name",
)

def android_library_instrumentation_tests(name, srcs, deps, target_devices, custom_package = None, nocompress_extensions = None, manifest_values = {}, **kwargs):
    """A macro for an instrumentation test whose target under test is an android_library.

    Will generate a 'self-instrumentating' test binary and other associated rules

    The intent of this wrapper is to simplify the build API for creating instrumentation test rules
    for simple cases, while still supporting build_cleaner for automatic dependency management.

    This will generate:
      - a test_lib android_library, containing all sources and dependencies
      - a test_binary android_binary (soon to be android_application)
      - the manifest to use for the test library.
      - for each src + device combination:
         - an android_instrumentation_test rule)

    Args:
      name: the name to use for the generated android_library rule. This is needed for build_cleaner to
        manage dependencies
      srcs: the test sources to generate rules for
      deps: the build dependencies to use for the generated test binary
      target_devices: array of device targets to execute on
      custom_package: Optional. Package name of the library. It could be inferred if unset
      nocompress_extensions: Optional. A list of file extensions to leave uncompressed in the resource apk.
      manifest_values: Optional. A dictionary of values to be overridden in the manifest
      **kwargs: arguments to pass to generated android_instrumentation_test rules
    """
    library_name = "%s_library" % name
    android_package_name = custom_package
    if android_package_name == None:
      android_package_name = infer_android_package_name()

    native.android_binary(
        name = "target_stub_binary",
        manifest = "//build_extensions:AndroidManifest_target_stub.xml",
        manifest_values = {"applicationId": android_package_name},
        testonly = 1,
    )

    native.android_library(
        name = library_name,
        srcs = srcs,
        testonly = 1,
        deps = deps,
    )

    generate_instrumentation_tests(
        name = name,
        srcs = srcs,
        deps = [library_name],
        target_devices = target_devices,
        instruments = ":target_stub_binary",
        custom_package = custom_package,
        manifest_values = manifest_values,
        **kwargs
    )
