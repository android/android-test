"""Skylark rules to setup the WORKSPACE in the opensource bazel world."""

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# These dependencies are required for *developing* this project.
def _development_repositories():
    native.maven_jar(
        name = "jcommander",
        artifact = "com.beust:jcommander:1.72",
    )

    native.maven_jar(
        name = "junit",
        artifact = "junit:junit:4.12",
    )

    native.maven_jar(
        name = "hamcrest",
        artifact = "org.hamcrest:hamcrest-all:1.3",
    )

    native.maven_jar(
        name = "mockito",
        artifact = "org.mockito:mockito-core:1.9.5",
    )

    native.maven_jar(
        name = "objenesis",
        artifact = "org.objenesis:objenesis:2.1",
    )

    native.maven_jar(
        name = "dexmaker",
        artifact = "com.google.dexmaker:dexmaker:1.2",
    )

    native.maven_jar(
        name = "dexmaker_mockito",
        artifact = "com.google.dexmaker:dexmaker-mockito:jar:1.2",
    )

    native.maven_jar(
        name = "truth",
        artifact = "com.google.truth:truth:0.42",
    )

    native.maven_jar(
        name = "guava",
        artifact = "com.google.guava:guava:25.1-android",
    )

    native.maven_jar(
        name = "guice",
        artifact = "com.google.inject:guice:4.1.0",
    )

    native.maven_jar(
        name = "guice_multibindings",
        artifact = "com.google.inject.extensions:guice-multibindings:4.1.0",
    )

    native.maven_jar(
        name = "jsr305",
        artifact = "com.google.code.findbugs:jsr305:3.0.2",
    )

    native.maven_jar(
        name = "javax_inject",
        artifact = "javax.inject:javax.inject:1",
    )

    native.maven_jar(
        name = "javax_annotation",
        artifact = "javax.annotation:javax.annotation-api:1.3.1",
    )

    native.maven_jar(
        name = "tagsoup",
        artifact = "org.ccil/cowan.tagsoup:tagsoup:1.2",
    )

    http_archive(
        name = "robolectric",
        sha256 = "dff7a1f8e7bd8dc737f20b6bbfaf78d8b5851debe6a074757f75041029f0c43b",
        strip_prefix = "robolectric-bazel-4.0.1",
        urls = ["https://github.com/robolectric/robolectric-bazel/archive/4.0.1.tar.gz"],
    )
    # uncomment to test with new robolectric version. Change path to point to local filesystem
    # clone of https://github.com/robolectric/robolectric-bazel
    #native.local_repository(
    #    name = "robolectric",
    #    path = "~/robogithub/robolectric-bazel/",
    #)

    # java_lite_proto_library rules implicitly depend on @com_google_protobuf_javalite//:javalite_toolchain,
    # which is the JavaLite proto runtime (base classes and common utilities).
    http_archive(
        name = "com_google_protobuf_javalite",
        strip_prefix = "protobuf-javalite",
        urls = ["https://github.com/google/protobuf/archive/javalite.zip"],
    )

    native.maven_jar(
        name = "auto_value_value",
        artifact = "com.google.auto.value:auto-value:1.5.1",
    )

    native.maven_jar(
        name = "kxml",
        artifact = "net.sf.kxml:kxml2:jar:2.3.0",
    )

    native.maven_jar(
        name = "aop_alliance",
        artifact = "aopalliance:aopalliance:1.0",
    )

    http_archive(
        name = "jsr330",
        build_file_content = """
package(default_visibility = ["//visibility:public"])
java_import(
    name = "jsr330",
    jars = ["javax.inject.jar"],
)""",
        url = "https://github.com/javax-inject/javax-inject/releases/download/1/javax.inject.zip",
    )

    native.maven_jar(
        name = "dagger_api",
        artifact = "com.google.dagger:dagger:2.10",
    )

    native.maven_jar(
        name = "dagger_compiler",
        artifact = "com.google.dagger:dagger-compiler:2.11",
    )

    native.maven_jar(
        name = "dagger_producers",
        artifact = "com.google.dagger:dagger-producers:2.11",
    )

    native.maven_jar(
        name = "googlejavaformat",
        artifact = "com.google.googlejavaformat:google-java-format:1.4",
    )

    native.maven_jar(
        name = "errorprone_javac_shaded",
        artifact = "com.google.errorprone:javac-shaded:9-dev-r4023-3",
    )

    native.maven_jar(
        name = "javapoet",
        artifact = "com.squareup:javapoet:1.9.0",
    )

    native.maven_jar(
        name = "jarjar",
        artifact = "com.googlecode.jarjar:jarjar:1.3",
    )

    native.maven_jar(
        name = "accessibility",
        artifact = "com.google.android.apps.common.testing.accessibility.framework:accessibility-test-framework:2.0",
    )

# These dependencies are for *users* of the Android Test repo,
# i.e. specifying this repository as @androidx_test in their
# WORKSPACE using a repository_rule like git_repository or http_archive.
# Use parameter `with_dev_repositories = True` to download the dev
# repositories as well.
def android_test_repositories(with_dev_repositories = False):
    """Loads the workspace by downloading the required dependencies."""

    if with_dev_repositories:
        _development_repositories()

    # Several of the repositories here end in _archive. This is due to an issue
    # with the Bazel python rules that occurs if the repository name is the same
    # as the top level directory of that repository. For example, suppose //:a
    # depends on @b//:b and @b//:b is defined by
    #
    #     filegroup(
    #         name = "b",
    #         srcs = ["b/c.py"],
    #     )
    #
    # Then the execroot looks like
    #
    #     bazel-bin/
    #       - a
    #       - a.runfiles/
    #           - b/
    #               - b/
    #                   - c.py
    #                   - __init__.py
    #               - __init__.py
    #           - __main__/
    #               - a
    #               - a.py
    #           - __init__.py
    #
    # The Python path for a.py contains, in order, the following:
    #
    #     [a.runfiles, a.runfiles/b, a.runfiles/__main__]
    #
    # If a.py contains `from b import c`, then python will find a.runfiles on
    # the Python path, see that it contains both __init__.py and b/ but does not
    # contain c.py and will error out.
    #
    # On the other hand, if we call @b//:b @b_archive//:b, then the first entry on
    # the python path containing b/ is a.runfiles/b_archive which contains all of
    # __init__.py, b/ and b/c.py so the import will succeed.

    http_archive(
        name = "google_apputils",
        build_file = str(Label("//opensource:google-apputils.BUILD")),
        sha256 = "47959d0651c32102c10ad919b8a0ffe0ae85f44b8457ddcf2bdc0358fb03dc29",
        strip_prefix = "google-apputils-0.4.2",
        url = "https://pypi.python.org/packages/69/66/a511c428fef8591c5adfa432a257a333e0d14184b6c5d03f1450827f7fe7/google-apputils-0.4.2.tar.gz",
    )

    http_archive(
        name = "gflags_archive",
        build_file = str(Label("//opensource:gflags.BUILD")),
        sha256 = "3377d9dbeedb99c0325beb1f535f8fa9fa131d1d8b50db7481006f0a4c6919b4",
        strip_prefix = "python-gflags-3.1.0",
        url = "https://github.com/google/python-gflags/releases/download/3.1.0/python-gflags-3.1.0.tar.gz",
    )

    http_archive(
        name = "portpicker_archive",
        build_file = str(Label("//opensource:portpicker.BUILD")),
        sha256 = "2f88edf7c6406034d7577846f224aff6e53c5f4250e3294b1904d8db250f27ec",
        strip_prefix = "portpicker-1.1.1/src",
        url = "https://pypi.python.org/packages/96/48/0e1f20fdc0b85cc8722284da3c5b80222ae4036ad73210a97d5362beaa6d/portpicker-1.1.1.tar.gz",
    )

    http_archive(
        name = "mox_archive",
        build_file = str(Label("//opensource:mox.BUILD")),
        sha256 = "424ee725ee12652802b4e86571f816059b0d392401ceae70bf6487d65602cba9",
        strip_prefix = "mox-0.5.3",
        url = "https://pypi.python.org/packages/0c/a1/64740c638cc5fae807022368f4141700518ee343b53eb3e90bf3cc15a4d4/mox-0.5.3.tar.gz#md5=6de7371e7e8bd9e2dad3fef2646f4a43",
    )

    # Six provides simple utilities for wrapping over differences between Python 2 and Python 3.
    http_archive(
        name = "six_archive",
        build_file = str(Label("//opensource:six.BUILD")),
        sha256 = "105f8d68616f8248e24bf0e9372ef04d3cc10104f1980f54d57b2ce73a5ad56a",
        strip_prefix = "six-1.10.0",
        url = "https://pypi.python.org/packages/source/s/six/six-1.10.0.tar.gz",
    )

    # Needed by protobuf
    native.bind(name = "six", actual = "@six_archive//:six")

    http_archive(
        name = "com_google_protobuf",
        strip_prefix = "protobuf-3.6.1.2",
        sha256 = "2244b0308846bb22b4ff0bcc675e99290ff9f1115553ae9671eba1030af31bc0",
        urls = ["https://github.com/protocolbuffers/protobuf/archive/v3.6.1.2.tar.gz"],
    )

    # Open source version of the google python flags library.
    http_archive(
        name = "absl_py",
        sha256 = "980ce58c34dfa75a9d20d45c355658191c166557f1de41ab52f208bd00604c2b",
        strip_prefix = "abseil-py-b347ba6022370f895d3133241ed96965b95ecb40",
        urls = ["https://github.com/abseil/abseil-py/archive/b347ba6022370f895d3133241ed96965b95ecb40.tar.gz"],
    )
