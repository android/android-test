"""Internal helper function for generating a instrumentation test."""

load("//tools/build_defs/android:rules.bzl", "android_binary")
load(
    "//third_party/android/androidx_test/build_extensions:android_multidevice_instrumentation_test.bzl",
    "android_multidevice_instrumentation_test",
)

def generate_instrumentation_test(
        name,
        srcs,
        deps,
        target_devices,
        test_application_id,
        manifest_values,
        custom_package,
        instrumentation_target_package,
        instruments,
        binary_args = {},
        **kwargs):
    """A helper rule to generate instrumentation test.


    This will generate:
      - a test_binary android_binary (soon to be android_application)
      - the manifest to use for the test library.
      - for each device combination:
         - an android_instrumentation_test rule)

    Args:
      name: unique prefix to use for generated rules
      srcs: the test sources to generate rules for
      deps: the build dependencies to use for the generated test binary
      target_devices: array of device targets to execute on
      test_application_id: the application id to use for the test_app.
      instrumentation_target_package: the android package name to specify as instrumentationTargetPackage in the test_app manifest
      instruments: The android binary the tests instrument.
      binary_args: Optional additional arguments to pass to generated android_binary
      **kwargs: arguments to pass to generated android_instrumentation_test rules
    """

    _manifest_values = {
        "applicationId": test_application_id,
        "instrumentationTargetPackage": instrumentation_target_package,
    }
    manifest_values.update(_manifest_values)
    android_binary(
        name = "%s_binary" % name,
        instruments = instruments,
        manifest = "//third_party/android/androidx_test/build_extensions:AndroidManifest_instrumentation_test_template.xml",
        testonly = 1,
        manifest_values = manifest_values,
        deps = deps + [
            "//third_party/android/androidx_test/runner/android_junit_runner",
        ],
        **binary_args
    )
    native.test_suite(name = name, tags = ["%s_suite" % name])
    tags = kwargs.pop("tags", []) + ["%s_suite" % name]
    for src in srcs:
        if src.endswith(".java"):
            srcname = src.rstrip(".java")
        elif src.endswith(".kt"):
            # for some unknown reason rstrip(".kt") strips off too many chars
            srcname = src[:-3]
        else:
            fail("Unrecognized src %s. Expected .java or .kt file" % src)
        relative_class_name = srcname.replace("/", ".")
        android_multidevice_instrumentation_test(
            name = srcname,
            test_class = test_java_package_name + "." + relative_class_name,
            target_devices = target_devices,
            test_app = "%s_binary" % name,
            support_apps = [instruments] if instruments else [],
            tags = tags,
            **kwargs
        )
