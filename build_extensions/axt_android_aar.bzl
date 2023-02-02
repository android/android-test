"""Generate AXT android archive (aar)."""

load("//build_extensions:add_or_update_file_in_zip.bzl", "add_or_update_file_in_zip")
load("//build_extensions:combine_jars.bzl", "combine_jars")
load("//build_extensions:maven_info.bzl", "MavenFiles", "MavenInfo", "collect_maven_info")
load("//build_extensions:jarjar.bzl", "jarjar")
load("//build_extensions:copy_file.bzl", "copy_file")

def _android_aar_impl(ctx):
    # current_aar will include almost everything needed: an AndroidManifest.xml, compiled resources,
    # and a proguard.txt. However, its classes.jar will only include direct compiled srcs. Missing
    # will be any sources from transitive dependencies that also need to be bundled in the aar
    current_aar = ctx.attr.included_dep[AndroidLibraryAarInfo].aar
    if not current_aar:
        fail("included_dep %s does not produce an aar. Is it an android_library rule with an AndroidManifest.xml?" % ctx.attr.included_dep.label)

    if not ctx.attr.included_dep[MavenInfo].artifact:
        fail("Could not find maven artifact for included_dep %s. Does it have a maven_coordinates tag?")

    # build a combined classes jar from all dependencies that are part of this maven artifact
    included_runtime_jars = ctx.attr.included_dep[MavenInfo].transitive_included_runtime_jars.to_list()
    #print("%s" % included_runtime_jars)

    #print("included_runtime_jars = %s" % included_runtime_jars)
    classes_jar = ctx.actions.declare_file(ctx.attr.name + "_combined_classes.jar")
    combine_jars(
        ctx = ctx,
        input_jars = included_runtime_jars,
        output = classes_jar,
    )

    # optionally use jarjar to rename shaded dependencies
    if (ctx.attr.jarjar_rule):
        jarjar_classes_jar = ctx.actions.declare_file(ctx.attr.name + "_jarjar_classes.jar")
        jarjar(ctx, rule = ctx.file.jarjar_rule, src = classes_jar, out = jarjar_classes_jar)
        classes_jar = jarjar_classes_jar

    # update the aar with the new classes.jar
    add_or_update_file_in_zip(
        ctx,
        ctx.attr.name + "_classes",
        src = current_aar,
        out = ctx.outputs.aar,
        update_src = classes_jar,
        update_path = "classes.jar",
    )

    # produce src jar
    included_src_jars = ctx.attr.included_dep[MavenInfo].transitive_included_src_jars.to_list()

    #print("included_src_jars = %s" % included_src_jars)
    combine_jars(
        ctx = ctx,
        input_jars = included_src_jars,
        output = ctx.outputs.src_jar,
    )

    #print("artifact = %s" % str(ctx.attr.included_dep[MavenInfo].artifact))
    #print("maven_deps = %s" % str(ctx.attr.included_dep[MavenInfo].transitive_maven_direct_deps.to_list()))
    return [ctx.attr.included_dep[MavenInfo], MavenFiles(runtime = ctx.outputs.aar, src_jar = ctx.outputs.src_jar)]

axt_android_aar = rule(
    implementation = _android_aar_impl,
    attrs = {
        "included_dep": attr.label(
            doc = "The android_library target to use as a basis for the android archive. " +
                  "This must include an AndroidManifest.xml, and optionally resources, proguard_specs",
            mandatory = True,
            providers = [JavaInfo, AndroidLibraryAarInfo],
            aspects = [collect_maven_info],
        ),
        "jarjar_rule": attr.label(
            doc = "Optional file containing jarjar rules to be applied to the classes.",
            mandatory = False,
            allow_single_file = [".txt"],
        ),
        "_jdk": attr.label(
            default = Label("@bazel_tools//tools/jdk:current_java_runtime"),
            providers = [java_common.JavaRuntimeInfo],
        ),
        "_combine_jars_java": attr.label(
            executable = True,
            cfg = "exec",
            allow_files = True,
            default = Label("//build_extensions/jar_combiner/java/androidx/test/tools/jarcombiner"),
        ),
        "_jarjar": attr.label(
            default = Label("//build_extensions:jarjar_bin"),
            executable = True,
            cfg = "exec",
        ),
    },
    outputs = {
        "aar": "%{name}.aar",
        "src_jar": "%{name}-src.jar",
    },
)
