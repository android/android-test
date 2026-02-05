load("@rules_java//java:defs.bzl", "java_library")

package(default_applicable_licenses = ["//:license"])

java_library(
    name = "matching",
    srcs = ["matching/Matching.java"],
    visibility = [
        "//build_extensions/jar_validator/javatests:__subpackages__",
    ],
)

java_library(
    name = "notmatching",
    srcs = [
        "matching/Matching.java",
        "notmatching/NotMatching.java",
    ],
    visibility = [
        "//build_extensions/jar_validator/javatests:__subpackages__",
    ],
)
