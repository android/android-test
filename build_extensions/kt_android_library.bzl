"""Wrapper for android_library for bazel.
"""

load("@rules_kotlin//kotlin:android.bzl", _kt_android_library = "kt_android_library")

def kt_android_library(**kwargs):
    _kt_android_library(**kwargs)
