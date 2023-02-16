"""Internal helper function for generating instrumentation tests ."""

def generate_instrumentation_tests(
        name,
        srcs,
        deps,
        device_list,
        test_java_package_name,
        test_android_package_name,
        instrumentation_target_package,
        instruments,
        binary_args = {},
        **kwargs):
    """A helper rule to generate instrumentation tests.

    Currently unsupported in bazel


    This will generate:
      - a test_binary android_binary (soon to be android_application)
      - the manifest to use for the test library.
      - for each device combination:
         - an android_instrumentation_test rule)

    Args:
      name: unique prefix to use for generated rules
      srcs: the test sources to generate rules for
      deps: the build dependencies to use for the generated test binary
      device_list: list of device structs to execute on, generated from phone_devices.bzl:devices()
      test_java_package_name: the root java package name for the tests.
      test_android_package_name: the android package name to use for the android_binary test app. Typically this is the same as test_java_package_name
      instrumentation_target_package: the android package name to specify as instrumentationTargetPackage in the test_app manifest
      instruments: The android binary the tests instrument.
      binary_args: Optional additional arguments to pass to generated android_binary
      **kwargs: arguments to pass to generated android_instrumentation_test rules
    """
