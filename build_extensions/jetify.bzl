load("@build_bazel_rules_android//android:rules.bzl", "android_binary", "android_library")

def jetify_android_library(jetify_sources = False, **kwargs):
    # ignore, not supported in bazel
    android_library(**kwargs)

def jetify_android_binary(jetify_sources = False, **kwargs):
    # ignore, not supported in bazel
    android_binary(**kwargs)
