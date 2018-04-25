workspace(name = "androidx_test")

# Google Maven Repository
GMAVEN_COMMIT = "44d75d3e7bdfa8ff0b30ceb048b0f09bc6b72c70"

http_archive(
    name = "gmaven_rules",
    strip_prefix = "gmaven_rules-%s" % GMAVEN_COMMIT,
    urls = ["https://github.com/aj-michael/gmaven_rules/archive/%s.tar.gz" % GMAVEN_COMMIT],
)

load("@gmaven_rules//:gmaven.bzl", "gmaven_rules")

gmaven_rules()

android_sdk_repository(name = "androidsdk")

load("//:repo.bzl", "android_test_repositories")

android_test_repositories(with_dev_repositories = True)

load("@robolectric//bazel:setup_robolectric.bzl", "setup_robolectric")
setup_robolectric()

