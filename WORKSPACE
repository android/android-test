git_repository(
    name = 'gmaven_rules',
    remote = 'https://github.com/aj-michael/gmaven_rules',
    commit = '5e89b7cdc94d002c13576fad3b28b0ae30296e55',
)

load("@gmaven_rules//:gmaven.bzl", "gmaven_rules")

gmaven_rules()

android_sdk_repository(name = "androidsdk")

maven_jar(
    name = "junit",
    artifact = "junit:junit:4.12",
)

maven_jar(
    name = "accessibility",
    artifact = "com.google.android.apps.common.testing.accessibility.framework:accessibility-test-framework:2.0"
)

maven_jar(
    name = "jcommander",
    artifact = "com.beust:jcommander:1.72",
)

maven_jar(
    name = "guava",
    artifact = "com.google.guava:guava:23.5-android",
)

maven_jar(
    name = "guice",
    artifact = "com.google.inject:guice:4.1.0",
)

maven_jar(
    name = "guice_multibindings",
    artifact = "com.google.inject.extensions:guice-multibindings:4.1.0",
)

maven_jar(
    name = "jsr305",
    artifact = "com.google.code.findbugs:jsr305:3.0.2",
)

maven_jar(
    name = "javax_inject",
    artifact = "javax.inject:javax.inject:1",
)

maven_jar(
    name = "javax_annotation",
    artifact = "javax.annotation:javax.annotation-api:1.3.1",
)

maven_jar(
    name = "tagsoup",
    artifact = "org.ccil/cowan.tagsoup:tagsoup:1.2",
)

http_archive(
    name = "com_google_protobuf",
    strip_prefix = "protobuf-3.5.0",
    urls = ["https://github.com/google/protobuf/archive/v3.5.0.tar.gz"],
)

http_archive(
    name = "com_google_protobuf_java",
    strip_prefix = "protobuf-3.5.0",
    urls = ["https://github.com/google/protobuf/releases/download/v3.5.0/protobuf-java-3.5.0.tar.gz"],
)

# java_lite_proto_library rules implicitly depend on @com_google_protobuf_javalite//:javalite_toolchain,
# which is the JavaLite proto runtime (base classes and common utilities).
http_archive(
    name = "com_google_protobuf_javalite",
    strip_prefix = "protobuf-javalite",
    urls = ["https://github.com/google/protobuf/archive/javalite.zip"],
)

maven_jar(
    name = "auto_value_value",
    artifact = "com.google.auto.value:auto-value:1.5.1",
)

maven_jar(
    name = "kxml",
    artifact = "net.sf.kxml:kxml2:jar:2.3.0",
)

maven_jar(
    name = "aop_alliance",
    artifact = "aopalliance:aopalliance:1.0",
)

local_repository(
    name = "auto_value",
    path = "auto_value",
)

new_http_archive(
    name = "jsr330",
    build_file_content = """
package(default_visibility = ["//visibility:public"])
java_import(
    name = "jsr330",
    jars = ["javax.inject.jar"],
)""",
    url = "https://github.com/javax-inject/javax-inject/releases/download/1/javax.inject.zip",
)

# Open source version of the google python flags library.
http_archive(
    name = "absl_py",
    urls = ["https://github.com/abseil/abseil-py/archive/b347ba6022370f895d3133241ed96965b95ecb40.tar.gz"],
    strip_prefix = "abseil-py-b347ba6022370f895d3133241ed96965b95ecb40",
    sha256 = "980ce58c34dfa75a9d20d45c355658191c166557f1de41ab52f208bd00604c2b",
)

# Six provides simple utilities for wrapping over differences between Python 2 and Python 3.
new_http_archive(
    name = "six_archive",
    urls = [
        "http://mirror.bazel.build/pypi.python.org/packages/source/s/six/six-1.10.0.tar.gz",
        "https://pypi.python.org/packages/source/s/six/six-1.10.0.tar.gz",
    ],
    sha256 = "105f8d68616f8248e24bf0e9372ef04d3cc10104f1980f54d57b2ce73a5ad56a",
    strip_prefix = "six-1.10.0",
    build_file = "six.BUILD",
)

maven_jar(
    name = "dagger_api",
    artifact = "com.google.dagger:dagger:2.10",
)

maven_jar(
    name = "dagger_compiler",
    artifact = "com.google.dagger:dagger-compiler:2.11",
)

maven_jar(
    name = "dagger_producers",
    artifact = "com.google.dagger:dagger-producers:2.11",
)

maven_jar(
    name = "googlejavaformat",
    artifact = "com.google.googlejavaformat:google-java-format:1.4",
)

maven_jar(
    name = "errorprone_javac_shaded",
    artifact = "com.google.errorprone:javac-shaded:9-dev-r4023-3",
)

maven_jar(
    name = "javapoet",
    artifact = "com.squareup:javapoet:1.9.0",
)

maven_jar(
    name = "hamcrest",
    artifact = "org.hamcrest:hamcrest-all:1.3",
)

maven_jar(
    name = "jarjar",
    artifact = "com.googlecode.jarjar:jarjar:1.3",
)
