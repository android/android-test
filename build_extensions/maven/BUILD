licenses(["notice"])

package(default_visibility = [
    "//visibility:public",
])

# Used to generate a maven artifact.
sh_binary(
    name = "maven_artifact_sh",
    srcs = ["maven_artifact.sh"],
)

java_binary(
    name = "jarjar_bin",
    main_class = "org.pantsbuild.jarjar.Main",
    runtime_deps = ["@maven//:org_pantsbuild_jarjar"],
)
