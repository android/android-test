# TODO(b/114418172): rename to androidx_test. Requires a bazel change
workspace(name = "android_test_support")

# Google Maven Repository
GMAVEN_COMMIT = "dc905ebd5a306e2061351dcb6d3c787d84cfbac1"

http_archive(
    name = "gmaven_rules",
    strip_prefix = "gmaven_rules-%s" % GMAVEN_COMMIT,
    urls = ["https://github.com/aj-michael/gmaven_rules/archive/%s.tar.gz" % GMAVEN_COMMIT],
)

load("@gmaven_rules//:gmaven.bzl", "gmaven_rules")

gmaven_rules()

android_sdk_repository(
    name = "androidsdk",
    api_level = 28,
    build_tools_version = "28.0.3")

load("//:repo.bzl", "android_test_repositories")

android_test_repositories(with_dev_repositories = True)

load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")
robolectric_repositories()

# Kotlin toolchains
rules_kotlin_version = "c5e25d71af96d446af4a8cb283c261537fc9f64e"

http_archive(
    name = "io_bazel_rules_kotlin",
    urls = ["https://github.com/bazelbuild/rules_kotlin/archive/%s.zip" % rules_kotlin_version],
    type = "zip",
    strip_prefix = "rules_kotlin-%s" % rules_kotlin_version
)

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")
kotlin_repositories()
kt_register_toolchains()
