"""Utility for determing if given target is a kotlin target"""

load("@rules_kotlin//kotlin/internal:defs.bzl", "KtJvmInfo")

def is_kotlin(target):
    return KtJvmInfo in target
