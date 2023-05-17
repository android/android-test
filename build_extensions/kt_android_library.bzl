"""Wrapper for android_library for bazel.
"""

load("@io_bazel_rules_kotlin//kotlin:android.bzl", io_kt_android_library = "kt_android_library")

def kt_android_library(**kwargs):
    io_kt_android_library(**kwargs)
