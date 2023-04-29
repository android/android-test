# TODO(b/114418172): rename to androidx_test. Requires a bazel change
workspace(name = "android_test_support")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "4.5"

RULES_JVM_EXTERNAL_SHA = "b17d7388feb9bfa7f2fa09031b32707df529f26c91ab9e5d909eb1676badd9a6"

# This needs to be consistent with the KOTLIN_VERSION specified in build_extensions/axt_versions.bzl.
KOTLIN_VERSION = "1.7.22"

# Get from https://github.com/JetBrains/kotlin/releases/
KOTLINC_RELEASE_SHA = "9db4b467743c1aea8a21c08e1c286bc2aeb93f14c7ba2037dbd8f48adc357d83"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

# rules_proto defines proto_library.
http_archive(
    name = "rules_proto",
    sha256 = "2490dca4f249b8a9a3ab07bd1ba6eca085aaf8e45a734af92aad0c42d9dc7aaf",
    strip_prefix = "rules_proto-218ffa7dfa5408492dc86c01ee637614f8695c45",
    urls = [
        "https://github.com/bazelbuild/rules_proto/archive/218ffa7dfa5408492dc86c01ee637614f8695c45.tar.gz",
    ],
)

load("@rules_proto//proto:repositories.bzl", "rules_proto_dependencies", "rules_proto_toolchains")

rules_proto_dependencies()

rules_proto_toolchains()

http_archive(
    name = "com_google_protobuf_javalite",
    sha256 = "5d0f05587aa3ad56079b4c4481dcb462267e5f1075d905c321f8ed6339e74ab0",
    strip_prefix = "protobuf-22.3",
    urls = ["https://github.com/protocolbuffers/protobuf/releases/download/v22.3/protobuf-22.3.zip"],
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")
load(
    "//build_extensions:axt_versions.bzl",
    "ANDROIDX_ANNOTATION_EXPERIMENTAL_VERSION",
    "ANDROIDX_ANNOTATION_VERSION",
    "ANDROIDX_COMPAT_VERSION",
    "ANDROIDX_CONCURRENT_VERSION",
    "ANDROIDX_CORE_VERSION",
    "ANDROIDX_CURSOR_ADAPTER_VERSION",
    "ANDROIDX_DRAWER_LAYOUT_VERSION",
    "ANDROIDX_FRAGMENT_VERSION",
    "ANDROIDX_JUNIT_VERSION",
    "ANDROIDX_LEGACY_SUPPORT_VERSION",
    "ANDROIDX_LIFECYCLE_VERSION",
    "ANDROIDX_MULTIDEX_VERSION",
    "ANDROIDX_RECYCLERVIEW_VERSION",
    "ANDROIDX_TRACING_VERSION",
    "ANDROIDX_VIEWPAGER_VERSION",
    "ANDROIDX_WINDOW_VERSION",
    "CORE_VERSION",
    "GOOGLE_MATERIAL_VERSION",
    "GUAVA_LISTENABLEFUTURE_VERSION",
    "GUAVA_VERSION",
    "JUNIT_VERSION",
    "RUNNER_VERSION",
    "UIAUTOMATOR_VERSION",
)

maven_install(
    name = "maven",
    artifacts = [
        "androidx.annotation:annotation:" + ANDROIDX_ANNOTATION_VERSION,
        "androidx.annotation:annotation-experimental:jar:" + ANDROIDX_ANNOTATION_EXPERIMENTAL_VERSION,
        "androidx.appcompat:appcompat:" + ANDROIDX_COMPAT_VERSION,
        "androidx.concurrent:concurrent-futures:" + ANDROIDX_CONCURRENT_VERSION,
        "androidx.core:core:" + ANDROIDX_CORE_VERSION,
        "androidx.cursoradapter:cursoradapter:" + ANDROIDX_CURSOR_ADAPTER_VERSION,
        "androidx.drawerlayout:drawerlayout:" + ANDROIDX_DRAWER_LAYOUT_VERSION,
        "androidx.fragment:fragment:" + ANDROIDX_FRAGMENT_VERSION,
        "androidx.legacy:legacy-support-core-ui:" + ANDROIDX_LEGACY_SUPPORT_VERSION,
        "androidx.legacy:legacy-support-core-utils:" + ANDROIDX_LEGACY_SUPPORT_VERSION,
        "androidx.legacy:legacy-support-v4:" + ANDROIDX_LEGACY_SUPPORT_VERSION,
        "androidx.lifecycle:lifecycle-common:" + ANDROIDX_LIFECYCLE_VERSION,
        "androidx.multidex:multidex:" + ANDROIDX_MULTIDEX_VERSION,
        "androidx.recyclerview:recyclerview:" + ANDROIDX_RECYCLERVIEW_VERSION,
        "androidx.tracing:tracing:" + ANDROIDX_TRACING_VERSION,
        "androidx.test.uiautomator:uiautomator:" + UIAUTOMATOR_VERSION,
        "androidx.viewpager:viewpager:" + ANDROIDX_VIEWPAGER_VERSION,
        "androidx.window:window:" + ANDROIDX_WINDOW_VERSION,
        "androidx.window:window-java:" + ANDROIDX_WINDOW_VERSION,
        "aopalliance:aopalliance:1.0",
        "com.android.tools.lint:lint-api:30.1.0",
        "com.android.tools.lint:lint-checks:30.1.0",
        "com.beust:jcommander:1.72",
        maven.artifact(
            artifact = "accessibility-test-framework",
            exclusions = [
                # exclude the org.checkerframework dependency since that require
                # java8 compatibility. See b/176926990
                maven.exclusion(
                    artifact = "checker",
                    group = "org.checkerframework",
                ),
                # accessibility-test-framework depends on hamcrest 2.2 which causes 'Using type org.hamcrest.Matcher from an indirect dependency' compile errors
                maven.exclusion(
                    artifact = "hamcrest-core",
                    group = "org.hamcrest",
                ),
                maven.exclusion(
                    artifact = "hamcrest-library",
                    group = "org.hamcrest",
                ),
            ],
            group = "com.google.android.apps.common.testing.accessibility.framework",
            version = "3.1",
        ),
        "com.google.android.material:material:" + GOOGLE_MATERIAL_VERSION,
        "com.google.auto.value:auto-value:1.5.1",
        "com.google.code.findbugs:jsr305:3.0.2",
        "com.google.code.gson:gson:2.8.5",
        "com.google.dagger:dagger-compiler:2.38.1",
        "com.google.dagger:dagger-producers:2.38.1",
        "com.google.dagger:dagger:2.38.1",
        "com.google.errorprone:error_prone_annotations:2.9.0",
        "com.google.errorprone:javac-shaded:9-dev-r4023-3",
        "com.google.flogger:flogger-system-backend:0.4",
        "com.google.flogger:flogger:0.4",
        "com.google.flogger:google-extensions:0.4",
        "com.google.googlejavaformat:google-java-format:1.4",
        "com.google.guava:guava:" + GUAVA_VERSION,
        "com.google.guava:listenablefuture:" + GUAVA_LISTENABLEFUTURE_VERSION,
        "com.google.inject.extensions:guice-multibindings:4.1.0",
        "com.google.inject:guice:4.1.0",
        "com.google.truth:truth:1.0",
        "com.googlecode.jarjar:jarjar:1.3",
        "com.linkedin.dexmaker:dexmaker-mockito:jar:2.28.1",
        "com.linkedin.dexmaker:dexmaker:2.28.1",
        "com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0",
        "com.squareup:javapoet:1.9.0",
        "io.grpc:grpc-okhttp:1.54.1",
        "io.grpc:grpc-stub:1.54.1",
        "org.apache.tomcat:annotations-api:6.0.53",
        "javax.annotation:javax.annotation-api:1.3.1",
        "javax.inject:javax.inject:1",
        "joda-time:joda-time:2.10.1",
        "junit:junit:" + JUNIT_VERSION,
        "net.bytebuddy:byte-buddy-agent:1.9.10",
        "net.bytebuddy:byte-buddy:1.9.10",
        "net.sf.kxml:kxml2:jar:2.3.0",
        "org.ccil.cowan.tagsoup:tagsoup:1.2.1",
        "org.checkerframework:checker-compat-qual:2.5.5",
        "org.hamcrest:hamcrest-core:1.3",
        "org.hamcrest:hamcrest-library:1.3",
        "org.mockito:mockito-core:2.28.1",
        "org.objenesis:objenesis:2.6",
        "org.pantsbuild:jarjar:1.7.2",
        "org.jetbrains.kotlin:kotlin-stdlib:%s" % KOTLIN_VERSION,
        maven.artifact(
            artifact = "robolectric",
            exclusions = [
                # exclude the com.google.guava dependency since that require
                # java8 compatibility.
                maven.exclusion(
                    artifact = "guava",
                    group = "com.google.guava",
                ),
            ],
            group = "org.robolectric",
            version = "4.10",
        ),
    ],
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
        "https://dl.bintray.com/linkedin/maven",
    ],
)

# need to have a isolated version tree for listenablefuture, because otherwise
# listenablefuture will get resolved to 9999.0-empty-to-avoid-conflict-with-guava
maven_install(
    name = "maven_listenablefuture",
    artifacts = [
        "com.google.guava:listenablefuture:" + GUAVA_LISTENABLEFUTURE_VERSION,
    ],
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
        "https://dl.bintray.com/linkedin/maven",
    ],
)

android_sdk_repository(
    name = "androidsdk",
    api_level = 33,
    build_tools_version = "33.0.2",
)

load("//:repo.bzl", "android_test_repositories")

android_test_repositories(with_dev_repositories = True)

load("@robolectric//bazel:robolectric.bzl", "robolectric_repositories")

robolectric_repositories()

# Kotlin toolchains

rules_kotlin_version = "1.7.1"

rules_kotlin_sha = "fd92a98bd8a8f0e1cdcb490b93f5acef1f1727ed992571232d33de42395ca9b3"

http_archive(
    name = "io_bazel_rules_kotlin",
    sha256 = rules_kotlin_sha,
    urls = ["https://github.com/bazelbuild/rules_kotlin/releases/download/v%s/rules_kotlin_release.tgz" % rules_kotlin_version],
)

load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories", "kotlinc_version")

kotlin_repositories(
    compiler_release = kotlinc_version(
        release = KOTLIN_VERSION,
        sha256 = KOTLINC_RELEASE_SHA,
    ),
)

load("@io_bazel_rules_kotlin//kotlin:core.bzl", "kt_register_toolchains")

kt_register_toolchains()

# Android bazel rules from Dec 22 2022. This is the last commit that supports bazel 6.0.0
RULES_ANDROID_COMMIT = "ce37817d8589cac4a7cc20cb4d51fe8ad459dea1"

RULES_ANDROID_SHA = "402b1ed3756028dca11835dad3225689a4040c3b377de798709f9a39b5c6af17"

http_archive(
    name = "rules_android",
    sha256 = RULES_ANDROID_SHA,
    strip_prefix = "rules_android-%s" % RULES_ANDROID_COMMIT,
    url = "https://github.com/bazelbuild/rules_android/archive/%s.zip" % RULES_ANDROID_COMMIT,
)

load("@rules_android//:prereqs.bzl", "rules_android_prereqs")

rules_android_prereqs()

load("@rules_android//:defs.bzl", "rules_android_workspace")

rules_android_workspace()

register_toolchains(
    "@rules_android//toolchains/android:android_default_toolchain",
    "@rules_android//toolchains/android_sdk:android_sdk_tools",
)

# Updated 2023-02-01
http_archive(
    name = "rules_license",
    sha256 = "6157e1e68378532d0241ecd15d3c45f6e5cfd98fc10846045509fb2a7cc9e381",
    urls = [
        "https://github.com/bazelbuild/rules_license/releases/download/0.0.4/rules_license-0.0.4.tar.gz",
    ],
)
