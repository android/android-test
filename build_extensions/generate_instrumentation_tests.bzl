"""Internal helper function for generating instrumentation tests ."""

load(
    "//build_extensions:android_multidevice_instrumentation_test.bzl",
    "android_multidevice_instrumentation_test",
)
load(
    "//build_extensions:infer_android_package_name.bzl",
    "infer_android_package_name",
)

def generate_instrumentation_tests(
        name,
        srcs,
        deps,
        target_devices,
        instruments = None,
        custom_package = None,
        manifest_values = {},
        **kwargs):
    """A helper rule to generate instrumentation tests.


    This will generate:
      - a test_binary android_binary (soon to be android_application)
      - the manifest to use for the test library.
      - for each src + device combination:
         - an android_instrumentation_test rule)

    Args:
      name: unique prefix to use for generated rules
      srcs: the test sources to generate rules for
      deps: the build dependencies to use for the generated test binary
      target_devices: array of device targets to execute on
      android_package_name: Optional. Package name of the library. It could be inferred if unset
      nocompress_extensions: Optional. A list of file extensions to leave uncompressed in the resource apk.
      manifest_values: Optional. A dictionary of values to be overridden in the manifest
      **kwargs: arguments to pass to generated android_instrumentation_test rules
    """

    android_package_name = custom_package
    if android_package_name == None:
        android_package_name = infer_android_package_name()

    _manifest_values = {
        "applicationId": android_package_name + ".tests" if instruments else android_package_name,
        "instrumentationTargetPackage": android_package_name,
    }
    _manifest_values.update(manifest_values)
    native.android_binary(
        name = "%s_binary" % name,
        instruments = instruments,
        custom_package = custom_package,
        manifest = "//build_extensions:AndroidManifest_instrumentation_test_template.xml",
        manifest_values = _manifest_values,
        nocompress_extensions = kwargs.pop("nocompress_extensions", None),
        multidex = kwargs.pop("multidex", "off"),
        testonly = 1,
        deps = deps + [
            "//runner/android_junit_runner",
        ],
    )
    android_multidevice_instrumentation_test(
        name = "%s_tests" % name,
        target_devices = target_devices,
        test_app = "%s_binary" % name,
        **kwargs
    )
