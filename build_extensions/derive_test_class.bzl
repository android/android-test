"""A rule for deriving the test class argument for instrumentation tests."""

load(
    "//third_party/android/androidx_test/build_extensions:infer_java_package_name.bzl",
    "infer_java_package_name",
)

def derive_test_class(name, test_class = None):
    """Derive the test class arg for instrumentation tests.

    TODO: remove this when android_instrumentation_test supports
    test_class derivation directly

    Constructs the test_class value that informs the device test runner
    what test class to execute. Using this value will skip classpath
    scanning for tests and can thus provide a significant performance
    improvement.

    The intention is this macro should follow the same test_class construction
    algorithm as java_test and android_local_test.

    Args:
      name: the name of the test target. The test class name will be derived based on
        current source-root-relative-path + name. It will be unused if test_class is provided
      test_class: the custom test class name to use. Callers should specify this
        if their test is in a non standard source root or if the target name
        does not match the test class name.
    """
    if not test_class:
        # strip off any text after '-' or '_'
        name = name.partition("_")[0].partition("-")[0]
        test_class = infer_java_package_name() + "." + name

    return test_class
