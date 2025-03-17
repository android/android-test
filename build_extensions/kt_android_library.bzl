"""Wrapper for android_library for bazel.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", io_kt_android_library = "kt_android_library")

def kt_android_library(testonly = 1, **kwargs):
    # explicitly set testonly to 1 because io_kt_android_library doesn't seem to respect package(default_testonly = 1)
    io_kt_android_library(testonly = testonly, **kwargs)
