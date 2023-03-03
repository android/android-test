"""Utility for determing if given target is a kotlin target"""

load("@io_bazel_rules_kotlin//kotlin/internal:defs.bzl", "KtJvmInfo")

def is_kotlin(target):
    return KtJvmInfo in target
