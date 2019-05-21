# TODO(b/114418172): rename to androidx_test. Requires a bazel change
workspace(name = "android_test_support")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "2.1"

RULES_JVM_EXTERNAL_SHA = "515ee5265387b88e4547b34a57393d2bcb1101314bcc5360ec7a482792556f42"

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
        "aopalliance:aopalliance:1.0",
        "com.beust:jcommander:1.72",
        "com.google.android.apps.common.testing.accessibility.framework:accessibility-test-framework:2.0",
        "com.google.android.material:material:" + GOOGLE_MATERIAL_VERSION,
        "com.google.auto.value:auto-value:1.5.1",
        "com.google.code.findbugs:jsr305:3.0.2",
        "com.google.code.gson:gson:2.8.5",
        "com.google.dagger:dagger-compiler:2.11",
        "com.google.dagger:dagger-producers:2.11",
        "com.google.dagger:dagger:2.10",
        "com.google.errorprone:javac-shaded:9-dev-r4023-3",
        "com.google.flogger:flogger-system-backend:0.4",
        "com.google.flogger:flogger:0.4",
        "com.google.flogger:google-extensions:0.4",
        "com.google.googlejavaformat:google-java-format:1.4",
        "com.google.guava:guava:25.1-android",
        "com.google.guava:guava-testlib:25.1-android",
        "com.google.inject.extensions:guice-multibindings:4.1.0",
        "com.google.inject:guice:4.1.0",
        "com.google.truth:truth:0.44",
        "com.googlecode.jarjar:jarjar:1.3",
        "com.linkedin.dexmaker:dexmaker-mockito:jar:2.25.0",
        "com.linkedin.dexmaker:dexmaker:2.25.0",
        "com.squareup:javapoet:1.9.0",
        "javax.annotation:javax.annotation-api:1.3.1",
        "javax.inject:javax.inject:1",
        "joda-time:joda-time:2.10.1",
        "junit:junit:4.12",
        "net.bytebuddy:byte-buddy-agent:1.9.11",
        "net.bytebuddy:byte-buddy:1.9.11",
        "net.sf.kxml:kxml2:jar:2.3.0",
        "org.ccil/cowan.tagsoup:tagsoup:1.2",
        "org.checkerframework:checker-compat-qual:2.5.5",
        "org.hamcrest:hamcrest-all:1.3",
        "org.mockito:mockito-core:2.25.0",
        "org.objenesis:objenesis:2.1",
        "org.pantsbuild:jarjar:1.7.2",
    ],
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
        "https://dl.bintray.com/linkedin/maven",
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