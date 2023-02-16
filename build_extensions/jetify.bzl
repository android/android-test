load("@build_bazel_rules_android//android:rules.bzl", "android_library")

def jetify_android_library(**kwargs):
    # ignore, not supported in bazel
    android_library(**kwargs)
