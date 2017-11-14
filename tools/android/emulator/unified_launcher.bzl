"""Skylark rules to setup the WORKSPACE in the opensource bazel world."""

def load_workspace():
  """Loads the workspace by downloading the required dependencies."""

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

  native.new_http_archive(
      name = "google_apputils",
      url = "https://pypi.python.org/packages/69/66/a511c428fef8591c5adfa432a257a333e0d14184b6c5d03f1450827f7fe7/google-apputils-0.4.2.tar.gz",
      sha256 = "47959d0651c32102c10ad919b8a0ffe0ae85f44b8457ddcf2bdc0358fb03dc29",
      strip_prefix = "google-apputils-0.4.2",
      build_file = str(Label("//tools/android/emulator:google-apputils.BUILD")),
  )

  native.new_http_archive(
      name = "gflags_archive",
      url = "https://github.com/google/python-gflags/releases/download/3.1.0/python-gflags-3.1.0.tar.gz",
      sha256 = "3377d9dbeedb99c0325beb1f535f8fa9fa131d1d8b50db7481006f0a4c6919b4",
      strip_prefix = "python-gflags-3.1.0",
      build_file = str(Label("//tools/android/emulator:gflags.BUILD")),
  )

  native.new_http_archive(
      name = "portpicker_archive",
      url = "https://pypi.python.org/packages/96/48/0e1f20fdc0b85cc8722284da3c5b80222ae4036ad73210a97d5362beaa6d/portpicker-1.1.1.tar.gz",
      sha256 = "2f88edf7c6406034d7577846f224aff6e53c5f4250e3294b1904d8db250f27ec",
      strip_prefix = "portpicker-1.1.1/src",
      build_file = str(Label("//tools/android/emulator:portpicker.BUILD")),
  )

  native.new_http_archive(
      name = "mox_archive",
      url = "https://pypi.python.org/packages/0c/a1/64740c638cc5fae807022368f4141700518ee343b53eb3e90bf3cc15a4d4/mox-0.5.3.tar.gz#md5=6de7371e7e8bd9e2dad3fef2646f4a43",
      sha256 = "424ee725ee12652802b4e86571f816059b0d392401ceae70bf6487d65602cba9",
      strip_prefix = "mox-0.5.3",
      build_file = str(Label("//tools/android/emulator:mox.BUILD"))
  )

  native.new_http_archive(
      name = "six_archive",
      url = "https://pypi.python.org/packages/source/s/six/six-1.10.0.tar.gz",
      sha256 = "105f8d68616f8248e24bf0e9372ef04d3cc10104f1980f54d57b2ce73a5ad56a",
      strip_prefix = "six-1.10.0",
      build_file = str(Label("//tools/android/emulator:six.BUILD")),
  )

  # Needed by protobuf
  native.bind(name = "six", actual = "@six_archive//:six")

  native.new_http_archive(
      name = "protobuf",
      url = "https://github.com/google/protobuf/releases/download/v3.4.1/protobuf-python-3.4.1.tar.gz",
      sha256 = "1faa722cf475c8e4c43ddb393d6f1477f1a56c93be38a1c8e367c358db476b5f",
      strip_prefix = "protobuf-3.4.1",
      # TODO(https://github.com/google/protobuf/issues/2833): Remove BUILD file
      # and make this rule native.http_archive. This will also require updating
      # the URL to be a newer protobuf release.
      build_file = str(Label("//tools/android/emulator:protobuf.BUILD")),
  )
