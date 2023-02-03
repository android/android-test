"""Generate AXT android archive (aar)."""

load("//build_extensions/maven:add_or_update_file_in_zip.bzl", "add_or_update_file_in_zip")
load("//build_extensions/maven:combine_jars.bzl", "combine_jars")
load("//build_extensions/maven:maven_info.bzl", "MavenFiles", "MavenInfo")
load("//build_extensions/maven:jarjar.bzl", "jarjar")
load("//build_extensions/maven:copy_file.bzl", "copy_file")

def _axt_maven_apk_impl(ctx):
    # produce src jar
    included_src_jars = ctx.attr.included_dep[JavaInfo].transitive_source_jars.to_list()

    combine_jars(
        ctx = ctx,
        input_jars = included_src_jars,
        output = ctx.outputs.src_jar,
    )

    maven_info = MavenInfo(
        artifact = ctx.attr.artifact,
        is_compileOnly = False,
        is_shaded = False,
        transitive_included_runtime_jars = depset(),
        transitive_included_src_jars = depset(),
        transitive_maven_direct_deps = depset(ctx.attr.maven_deps),
    )

    return [maven_info, MavenFiles(runtime = ctx.attr.included_dep[ApkInfo].signed_apk, src_jar = ctx.outputs.src_jar)]

axt_maven_apk = rule(
    implementation = _axt_maven_apk_impl,
    attrs = {
        "included_dep": attr.label(
            doc = "The android_binary to publish",
            mandatory = True,
            providers = [JavaInfo, ApkInfo],
        ),
        "artifact": attr.string(
            doc = "the maven coordinates of the apk",
            mandatory = True,
        ),
        "maven_deps": attr.string_list(
            doc = "the maven coordinates of the runtime dependencies of the apk",
        ),
        "_combine_jars_java": attr.label(
            executable = True,
            cfg = "exec",
            allow_files = True,
            default = Label("//build_extensions/jar_combiner/java/androidx/test/tools/jarcombiner"),
        ),
    },
    outputs = {
        "src_jar": "%{name}-src.jar",
    },
)
