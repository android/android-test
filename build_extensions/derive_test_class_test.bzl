"""Unit tests for derive_test_class."""

load("//third_party/android/androidx_test/build_extensions:derive_test_class.bzl", "derive_test_class_from_srcs")
load("//third_party/bazel_skylib/lib:unittest.bzl", "asserts", "unittest")

def _derive_test_class_test_impl(ctx):
    env = unittest.begin(ctx)

    asserts.equals(env, ".FooTest", derive_test_class_from_srcs(["FooTest.java"]))
    asserts.equals(env, ".FooTest", derive_test_class_from_srcs(["FooTest.kt"]))
    asserts.equals(env, "androidx.test.runner.suites.AndroidClasspathSuite", derive_test_class_from_srcs(["FooTest.kt", "BarTest.kt"]))
    asserts.equals(env, "androidx.test.runner.suites.AndroidClasspathSuite", derive_test_class_from_srcs([]))
    asserts.equals(env, ".bar.FooTest", derive_test_class_from_srcs(["bar/FooTest.kt"]))

    return unittest.end(env)

# TODO: create test for failure case where test_class cannot be derived

derive_test_class_test = unittest.make(
    impl = _derive_test_class_test_impl,
)
