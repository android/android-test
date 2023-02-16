# Essential dagger libraries for AXT in Bazel

package(default_visibility = ["//visibility:public"])

java_library(
    name = "dagger",
    exported_plugins = [":dagger_plugin"],
    exports = [
        "@maven//:com_google_dagger_dagger",
        "@maven//:javax_inject_javax_inject",
    ],
)

java_plugin(
    name = "dagger_plugin",
    generates_api = 1,
    processor_class = "dagger.internal.codegen.ComponentProcessor",
    deps = [
        "@maven//:com_google_dagger_dagger",
        "@maven//:com_google_dagger_dagger_compiler",
        "@maven//:com_google_dagger_dagger_producers",
        "@maven//:com_google_errorprone_javac_shaded",
        "@maven//:com_google_googlejavaformat_google_java_format",
        "@maven//:com_google_guava_guava",
        "@maven//:com_squareup_javapoet",
        "@maven//:javax_annotation_javax_annotation_api",
        "@maven//:javax_inject_javax_inject",
    ],
)
