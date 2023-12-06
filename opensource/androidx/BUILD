# Targets to handle androidx dependencies in bazel

package(default_visibility = ["//visibility:public"])

load(
    "//build_extensions:axt_deps_versions.bzl",
    "ANDROIDX_ANNOTATION_VERSION",
)

# Alias target for androidx.annotation
# Ideally libraries could directly depend on the @maven targets instead, but:
# 1. Directly depending on annotation doesn't work since it contains no classes,
# and results in 'error: [strict] Using type androidx.annotation.RestrictTo
# from an indirect dependency (TOOL_INFO:
# "@maven//:androidx_annotation_annotation_jvm").'
# 2. Directly depending on annotation-jvm doesn't work since that can result in
# duplicate class errors when transitive dependencies depend on older versions
# of androidx.annotation
java_library(
    name = "annotation",
    tags = [
        "maven_coordinates=androidx.annotation:annotation:" + ANDROIDX_ANNOTATION_VERSION,
    ],
    exports = [
        "@maven//:androidx_annotation_annotation",
        "@maven//:androidx_annotation_annotation_jvm",
    ],
)
