android_sdk_repository(name = "androidsdk")

maven_jar(
    name = "junit",
    artifact = "junit:junit:4.12",
)

maven_jar(
    name = "jcommander",
    artifact = "com.beust:jcommander:1.72",
)

maven_jar(
    name = "guava",
    artifact = "com.google.guava:guava:23.0",
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

http_archive(
    name = "com_google_protobuf",
    strip_prefix = "protobuf-3.5.0",
    urls = ["https://github.com/google/protobuf/archive/v3.4.1.tar.gz"],
)

http_archive(
    name = "com_google_protobuf_java",
    strip_prefix = "protobuf-3.4.1",
    urls = ["https://github.com/google/protobuf/releases/download/v3.4.1/protobuf-java-3.4.1.tar.gz"],
)

maven_jar(
    name = "auto_value_value",
    artifact = "com.google.auto.value:auto-value:1.5.1",
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
