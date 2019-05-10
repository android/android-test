# TODO(b/114418172): rename to androidx_test. Requires a bazel change
workspace(name = "android_test_support")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "1.2"

RULES_JVM_EXTERNAL_SHA = "e5c68b87f750309a79f59c2b69ead5c3221ffa54ff9496306937bfa1c9c8c86b"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load(
    "//build_extensions:axt_versions.bzl",
    "ANDROIDX_JUNIT_VERSION",
    "ANDROIDX_LIFECYCLE_VERSION",
    "ANDROIDX_MULTIDEX_VERSION",
    "ANDROIDX_VERSION",
    "CORE_VERSION",
    "GOOGLE_MATERIAL_VERSION",
    "RUNNER_VERSION",
    "UIAUTOMATOR_VERSION",
)

maven_install(
    name = "maven",
    artifacts = [
        "androidx.annotation:annotation:" + ANDROIDX_VERSION,
        "androidx.appcompat:appcompat:" + ANDROIDX_VERSION,
        "androidx.core:core:" + ANDROIDX_VERSION,
        "androidx.cursoradapter:cursoradapter:" + ANDROIDX_VERSION,
        "androidx.drawerlayout:drawerlayout:" + ANDROIDX_VERSION,
        "androidx.fragment:fragment:" + ANDROIDX_VERSION,
        "androidx.legacy:legacy-support-core-ui:" + ANDROIDX_VERSION,
        "androidx.legacy:legacy-support-core-utils:" + ANDROIDX_VERSION,
        "androidx.legacy:legacy-support-v4:" + ANDROIDX_VERSION,
        "androidx.lifecycle:lifecycle-common:" + ANDROIDX_LIFECYCLE_VERSION,
        "androidx.multidex:multidex:" + ANDROIDX_MULTIDEX_VERSION,
        "androidx.recyclerview:recyclerview:" + ANDROIDX_VERSION,
        "androidx.test.uiautomator:uiautomator:" + UIAUTOMATOR_VERSION,        
        "androidx.viewpager:viewpager:" + ANDROIDX_VERSION,
        "com.google.android.material:material:" + GOOGLE_MATERIAL_VERSION,
        "org.pantsbuild:jarjar:1.7.2",
    ],
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
)

android_sdk_repository(
    name = "androidsdk",
    api_level = 28,
    build_tools_version = "28.0.3",
)

load("//:repo.bzl", "android_test_repositories")

android_test_repositories(with_dev_repositories = True)

load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")

robolectric_repositories()

# Kotlin toolchains
rules_kotlin_version = "4c71740a1b63b785fc90afd8d4d4d5bfda527107"

http_archive(
    name = "io_bazel_rules_kotlin",
    sha256 = "c0ca7b66d9f466067635482592634703bf0a648d51ec958f41796d43ca8256b3",
    strip_prefix = "rules_kotlin-%s" % rules_kotlin_version,
    type = "zip",
    urls = ["https://github.com/bazelbuild/rules_kotlin/archive/%s.zip" % rules_kotlin_version],
)

load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kotlin_repositories", "kt_register_toolchains")

kotlin_repositories()

kt_register_toolchains()
