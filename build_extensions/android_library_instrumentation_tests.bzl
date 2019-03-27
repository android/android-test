"""A rule wrapper for an instrumentation test for an android library."""

load(
    "//build_extensions:android_multidevice_instrumentation_test.bzl",
    "android_multidevice_instrumentation_test",
)
load(
    "//build_extensions:infer_android_package_name.bzl",
    "infer_android_package_name",
)

def android_library_instrumentation_tests(name, srcs, deps, target_devices, **kwargs):
    """A rule for an instrumentation test whose target under test is an android_library.

    Will generate a 'self-instrumentating' test binary and other associated rules

    The intent of this wrapper is to simplify the build API for creating instrumentation test rules
    for simple cases, while still supporting build_cleaner for automatic dependency management.

    This will generate:
      - a stub target binary. Essentially unused but blaze/bazel require it
      - a test_lib android_library, containing all sources and dependencies
      - a test_binary android_binary (soon to be android_application)
      - the manifest to use for the test library.
      - for each device:
         - a android_instrumentation_test rule

    Args:
      name: the name to use for the generated android_library rule. This is needed for build_cleaner to
        manage dependencies
      srcs: the test sources to generate rules for
      deps: the build dependencies to use for the generated test binary
      target_devices: array of device targets to execute on
      **kwargs: arguments to pass to generated android_instrumentation_test rules
    """
    library_name = name
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
    native.android_binary(
        name = "%s_binary" % library_name,
        instruments = ":target_stub_binary",
        manifest = "//build_extensions:AndroidManifest_instrumentation_test_template.xml",
        manifest_values = {
            "applicationId": android_package_name,
            "instrumentationTargetPackage": android_package_name,
        },
        testonly = 1,
        deps = [name],
    )
    android_multidevice_instrumentation_test(
        name = "%s_tests" % library_name,
        target_devices = target_devices,
        test_app = ":%s_binary" % library_name,
        **kwargs
    )
