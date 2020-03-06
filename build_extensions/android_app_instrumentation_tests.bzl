"""A rule wrapper for an instrumentation test for an android binary."""

load(
    "//build_extensions:generate_instrumentation_tests.bzl",
    "generate_instrumentation_tests",
)


def android_app_instrumentation_tests(name, binary_target, srcs, deps, target_devices, custom_package = None, manifest_values = {}, **kwargs):
    """A macro for an instrumentation test whose target under test is an android_binary.

    The intent of this wrapper is to simplify the build API for creating instrumentation test rules
    for simple cases, while still supporting build_cleaner for automatic dependency management.

    This will generate:
      - a test_lib android_library, containing all sources and dependencies
      - a test_binary android_binary (soon to be android_application)
      - the manifest to use for the test library.
      - for each device:
         - a android_instrumentation_test rule

    Args:
      name: the name to use for the generated android_library rule. This is needed for build_cleaner to
        manage dependencies
      binary_target: the android_binary under test
      srcs: the test sources to generate rules for
      deps: the build dependencies to use for the generated test library
      target_devices: array of device targets to execute on
      custom_package: Optional. Package name of the library. It could be inferred if unset
      manifest_values: Optional. A dictionary of values to be overridden in the manifest
      **kwargs: arguments to pass to generated android_instrumentation_test rules
    """
    library_name = "%s_library" % name

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
        instruments = binary_target,
        custom_package = custom_package,
        manifest_values = manifest_values,
        **kwargs
    )
