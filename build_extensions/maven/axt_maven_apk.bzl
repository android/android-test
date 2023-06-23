"""Generate AXT android archive (aar)."""

load("//build_extensions/maven:combine_jars.bzl", "combine_jars")
load("//build_extensions/maven:maven_info.bzl", "MavenFilesInfo", "MavenInfo", "collect_maven_apk_info")

def _axt_maven_apk_impl(ctx):
    # produce src jar
    combine_jars(
        ctx = ctx,
        input_jars_deps = ctx.attr.included_dep[MavenInfo].transitive_included_src_jars,
        output = ctx.outputs.src_jar,
    )

    return [
        ctx.attr.included_dep[MavenInfo],
        MavenFilesInfo(runtime = ctx.attr.included_dep[ApkInfo].signed_apk, src_jar = ctx.outputs.src_jar, validation = None),
    ]

axt_maven_apk = rule(
    implementation = _axt_maven_apk_impl,
    attrs = {
        "included_dep": attr.label(
            doc = "The android_binary to publish",
            mandatory = True,
            providers = [JavaInfo, ApkInfo],
            aspects = [collect_maven_apk_info],
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
