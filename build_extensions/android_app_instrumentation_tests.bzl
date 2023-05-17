"""A rule wrapper for an instrumentation test for an android binary."""

load("//build_extensions:kt_android_library.bzl", "kt_android_library")
load(
    "//build_extensions:generate_instrumentation_tests.bzl",
    "generate_instrumentation_tests",
)
load(
    "//build_extensions:infer_java_package_name.bzl",
    "infer_java_package_name",
    "infer_java_package_name_from_label",
)
load("//build_extensions:register_extension_info.bzl", "register_extension_info")

def android_app_instrumentation_tests(
        name,
        binary_target,
        srcs,
        deps,
        device_list = [],
        test_java_package = None,
        binary_target_package = None,
        library_args = {},
        binary_args = {},
        **kwargs):
    """A macro for an instrumentation test whose target under test is an android_binary.

    The intent of this wrapper is to simplify the build API for creating instrumentation test rules
    for simple cases, while still supporting build_cleaner for automatic dependency management.

    This will generate:
      - a test_lib android_library, containing all sources and dependencies
      - a test_binary android_binary (soon to be android_application)
      - the manifest to use for the test library.
      - for each src + device combination:
         - a android_instrumentation_test rule

    Args:
      name: the name to use for the generated android_library rule. This is needed for build_cleaner to
        manage dependencies
      binary_target: the android_binary under test
      srcs: the test sources to generate rules for
      deps: the build dependencies to use for the generated test library
      device_list: list of device structs to execute on, generated from phone_devices.bzl:devices().
          By default this method returns a device for each available API level
      test_java_package: Optional. A custom root package name to use for the tests. If unset
          will be derived based on current path to a java source root
      binary_target_package: Optional: the android package name of binary_target. If unset, will be
          derived from binary target's path to a java source root
      library_args: additional arguments to pass to generated android_library
      binary_args: additional arguments to pass to generated android_binary
      **kwargs: arguments to pass to generated android_instrumentation_test rules
    """
    library_name = "%s_library" % name
    test_java_package_name = test_java_package if test_java_package else infer_java_package_name()
    instrumentation_target_package = binary_target_package if binary_target_package else infer_java_package_name_from_label(binary_target)

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
        # always append .tests at the end to avoid potential conflict with instrumentation_target_package
        test_android_package_name = instrumentation_target_package + ".tests",
        instrumentation_target_package = instrumentation_target_package,
        instruments = binary_target,
        binary_args = binary_args,
        **kwargs
    )

# registers the wrapper with build_cleaner so it can manage dependencies automatically
register_extension_info(
    extension = android_app_instrumentation_tests,
    label_regex_for_dep = "{extension_name}_library",
)
