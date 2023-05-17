"""A rule wrapper for an instrumentation test for an android library."""

load("//build_extensions:kt_android_library.bzl", "kt_android_library")
load(
    "//build_extensions:generate_instrumentation_tests.bzl",
    "generate_instrumentation_tests",
)
load(
    "//build_extensions:infer_java_package_name.bzl",
    "infer_java_package_name",
)
load("//build_extensions:register_extension_info.bzl", "register_extension_info")

def android_library_instrumentation_tests(
        name,
        srcs,
        deps,
        target_devices = [],
        device_list = [],
        test_java_package = None,
        library_args = {},
        binary_args = {},
        **kwargs):
    """DEPRECATED: use axt_android_library_test instead


    A macro for an instrumentation test whose target under test is an android_library.

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
      device_list: list of device structs to execute on,  generated from phone_devices.bzl:devices()
           By default this method returns a device for each available API level
      test_java_package: Optional. A custom root package name to use for the tests. If unset
          will be derived based on current path to a java source root
      library_args: additional arguments to pass to generated android_library
      binary_args: additional arguments to pass to generated android_binary
      **kwargs: arguments to pass to generated android_instrumentation_test rules
    """
    library_name = "%s_library" % name
    test_java_package_name = test_java_package if test_java_package else infer_java_package_name()

    kt_android_library(
        name = library_name,
        srcs = srcs,
        testonly = 1,
        deps = deps,
        **library_args
    )

    generate_instrumentation_tests(
        name = name,
        srcs = srcs,
        deps = [library_name],
        device_list = device_list,
        test_java_package_name = test_java_package_name,
        test_android_package_name = test_java_package_name,
        instrumentation_target_package = test_java_package_name,
        instruments = None,
        binary_args = binary_args,
        **kwargs
    )

# registers the wrapper with build_cleaner so it can manage dependencies automatically
register_extension_info(
    extension = android_library_instrumentation_tests,
    label_regex_for_dep = "{extension_name}_library",
)
